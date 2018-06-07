package bwfdm.sara.publication.dspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
//import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.Deposit;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.EntryPart;
import org.swordapp.client.ProtocolViolationException;
import org.swordapp.client.SWORDClient;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDCollection;
import org.swordapp.client.SWORDError;
import org.swordapp.client.SWORDWorkspace;
import org.swordapp.client.ServiceDocument;
import org.swordapp.client.UriRegistry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.Hierarchy;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.dspace.dto.v6.CollectionObject;
import bwfdm.sara.publication.dspace.dto.v6.HierarchyObject;
import bwfdm.sara.utils.JsonUtils;
import bwfdm.sara.utils.WebUtils;

public class DSpace_v6 implements PublicationRepository{
	protected static final Logger logger = LoggerFactory.getLogger(DSpace_v6.class);

	private final String /*rest_user, rest_pwd, */rest_api_endpoint;
	private final String sword_user, sword_pwd, sword_api_endpoint, sword_servicedocument;
	private final Repository dao;
		
	// For REST
	private final WebTarget restWebTarget;
	private final WebTarget collectionsWebTarget;
	private final WebTarget hierarchyWebTarget;
	private Client client;
	
	@JsonCreator
	public DSpace_v6(@JsonProperty("rest_user") final String ru, @JsonProperty("rest_pwd") final String rp,
			@JsonProperty("rest_api_endpoint") final String re, @JsonProperty("sword_user") final String su,
			@JsonProperty("sword_pwd") final String sp, @JsonProperty("sword_api_endpoint") final String se,
			@JsonProperty("dao") final Repository dao) {

		this.dao = dao;

		if (dao.url.endsWith("/"))
			throw new IllegalArgumentException("url must not end with slash: " + dao.url);

		rest_api_endpoint = dao.url + "/" + re;
		sword_user = su;
		sword_pwd = sp;
		sword_api_endpoint = dao.url + "/" + se;
		sword_servicedocument = sword_api_endpoint + "/servicedocument";
		
		client = WebUtils.getClientWithoutSSL(); // Ignore SSL-Verification
		//client = ClientBuilder.newClient();
		
		// WebTargets
		restWebTarget = client.target(rest_api_endpoint);
		collectionsWebTarget = restWebTarget.path("collections");
		hierarchyWebTarget = restWebTarget.path("hierarchy");
	}

	public DSpace_v6(String serviceDocumentURL, String restURL, String saraUser, String saraPassword) {
		sword_servicedocument = serviceDocumentURL;
		sword_user = saraUser;
		sword_pwd = saraPassword;
		sword_api_endpoint = null;
		rest_api_endpoint = restURL;
		dao = null;
		
		client = WebUtils.getClientWithoutSSL(); // Ignore SSL-Verification
		//client = ClientBuilder.newClient();
		
		restWebTarget = client.target(rest_api_endpoint);
		collectionsWebTarget = restWebTarget.path("collections");
		hierarchyWebTarget = restWebTarget.path("hierarchy");
	}

	/**
	 * Get service document via SWORD v2
	 * 
	 * @param swordClient
	 * @param serviceDocumentURL
	 * @param authCredentials
	 * @return ServiceDocument or null in case of error/exception
	 */
	private ServiceDocument getServiceDocument(SWORDClient swordClient, String serviceDocumentURL, AuthCredentials authCredentials) {
		ServiceDocument serviceDocument = null;
		try {
			serviceDocument = swordClient.getServiceDocument(sword_servicedocument, authCredentials);
		} catch (SWORDClientException | ProtocolViolationException e) {
			logger.error("Exception by accessing service document: " 
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
		return serviceDocument;
	}
	
	
	/**
	 * Get available collections via SWORD v2
	 * 
	 * @return Map<String, String> where key=URL, value=Title
	 */
	private Map<String, String> getAvailableCollectionsViaSWORD(AuthCredentials authCredentials){
		Map<String, String> collections = new HashMap<String, String>();
		SWORDClient swordClient = new SWORDClient();
		ServiceDocument serviceDocument = this.getServiceDocument(swordClient, sword_servicedocument, authCredentials);
		
		for(SWORDWorkspace workspace : serviceDocument.getWorkspaces()) {
			for (SWORDCollection collection : workspace.getCollections()) {
				// key = full URL, value = Title
				collections.put(collection.getHref().toString(), collection.getTitle());
			}
		}
		return collections;
	}
	
	
	private String getFileExtension(String fileName) {
		
		String extension = "";
		int i = fileName.lastIndexOf('.');
		if(i>0) {
			extension = fileName.substring(i+1);
		}
		return extension;		
	}
	
	
	private String getPackageFormat(String fileName) {
		String extension = this.getFileExtension(fileName);
		
		if(extension.toLowerCase().equals("zip")) {
			return UriRegistry.PACKAGE_SIMPLE_ZIP;
		}
		return UriRegistry.PACKAGE_BINARY;
	}
	
	/**
	 * Get response to the REST-request
	 * 
	 * @param webTarget
	 * @return
	 */
	private Response getResponse(WebTarget webTarget, String contentType, String acceptType) {
		final Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.header("Content-Type", contentType);
		invocationBuilder.header("Accept", acceptType);
		final Response response = invocationBuilder.get();
		return response;
	}
	
	
	//=== DSpace specific public methods ===
	
	
	/**
	 * Get a list of communities which determine the path to the collection
	 * Specific only for DSpace-6.
	 * <p>
	 * REST and SWORD requests are used.
	 * 
	 * @return a {@code List<String>} of communities (0 or more communities are possible) 
	 * 		   or {@code null} if a collection was not found
	 */
	public List<String> getCommunityListForCollection(String collectionURL){
		
		String collectionHandle = getCollectionHandle(collectionURL);
		if(collectionHandle == null) {
			return null;
		}
		
		List<String> communityList = new ArrayList<String>(0);
		
		final Response response = getResponse(hierarchyWebTarget, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
		final HierarchyObject hierarchy = JsonUtils.jsonStringToObject(
					WebUtils.readResponseEntity(String.class, response), HierarchyObject.class);
		
		// Get List of communities or "null", if collection is not found
		communityList = hierarchy.getCommunityListForCollection(hierarchy, collectionHandle, communityList);
		
		if(communityList != null) {
			communityList.remove(0); 	//remove "Workspace" - it is not a community, 
							    	   	//but it is in the first level of the hierarchy
		}
		return communityList; // List of communities ( >= 0) or "null"
	}
	
	/**
	 * Get a collection handle based on the collection URL.
	 * <p> 
	 * REST and SWORDv2 requests are used.
	 * 
	 * @param collectionURL
	 * @return String with a handle or {@code null} if collectionURL was not found 
	 */
	public String getCollectionHandle(String collectionURL) {
		System.out.println(collectionsWebTarget);
		
		String swordCollectionPath = ""; //collectionURL without a hostname and port
		
		// Find a collectionURL inside of all available collections. SWORD is used.
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd);
		ServiceDocument serviceDocument = this.getServiceDocument(swordClient, sword_servicedocument, authCredentials);
		
		for(SWORDWorkspace workspace : serviceDocument.getWorkspaces()) {
			for (SWORDCollection collection : workspace.getCollections()) {
				if(collection.getHref().toString().equals(collectionURL)) {
					swordCollectionPath = collection.getHref().getPath();
				}				
			}
		}	
		
		// Get all collections via REST to check, if swordCollectionPath contains a REST-handle
		final Response response = getResponse(collectionsWebTarget, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);				
		final CollectionObject[] collections = JsonUtils.jsonStringToObject(
					WebUtils.readResponseEntity(String.class, response), CollectionObject[].class);
		
		// Compare REST-handle and swordCollectionPath
		for(CollectionObject collection: collections) {
			if(swordCollectionPath.contains(collection.handle)) {
				return collection.handle;
			}
		}		
		return null; //collectionURL was not found	
	}
	
	
	/**
	 * Publish a file or metadata. Private method.
	 * <p>
	 * IMPORTANT - you can use ONLY 1 possibility in the same time (only file, or only metadata). 
	 * "Multipart" is not supported!
	 * 
	 * @param userLogin
	 * @param collectionURL - could be link to the collection (from the service document) 
	 * 		  or a link to edit the collection ("Location" field in the response)
	 * @param mimeFormat - use e.g. {@code "application/atom+xml"} or {@code "application/zip"}
	 * @param packageFormat - see {@link UriRegistry.PACKAGE_SIMPLE_ZIP} or {@linkplain UriRegistry.PACKAGE_BINARY}
	 * @param file
	 * @param metadataMap
	 * @return "Location" parameter from the response, or {@code null} in case of error
	 */
	private String publishElement(String userLogin, String collectionURL, String mimeFormat, String packageFormat, File file, Map<String, String> metadataMap) {
		
		// Check if only 1 parameter is used (metadata OR file). 
		// Multipart is not supported.
		if( ((file != null)&&(metadataMap != null)) || ((file == null)&&(metadataMap == null)) ) {
			return null; 
		}
		
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd, userLogin);
		
		Deposit deposit = new Deposit();
		
		try {
			// Check if "metadata as a Map"
			if(metadataMap != null) {
				EntryPart ep = new EntryPart();
				for(Map.Entry<String, String> metadataEntry : metadataMap.entrySet()) {
					ep.addDublinCore(metadataEntry.getKey(), metadataEntry.getValue());
				}
				deposit.setEntryPart(ep);
			}
			
			// Check if "file"
			if(file != null) {
				deposit.setFile(new FileInputStream(file));
				deposit.setFilename(file.getName()); 	// deposit works properly ONLY with a "filename" parameter 
														// --> in curl: -H "Content-Disposition: filename=file.zip"
			}
			
			deposit.setMimeType(mimeFormat);
			deposit.setPackaging(packageFormat);
			deposit.setInProgress(true);
			//deposit.setMd5("fileMD5");
			//deposit.setSuggestedIdentifier("abcdefg");
			
			DepositReceipt receipt = swordClient.deposit(collectionURL, deposit, authCredentials);
			return receipt.getLocation(); // "Location" parameter from the response
			
		} catch (FileNotFoundException e) {
			logger.error("Exception by accessing a file: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;	
		
		} catch (SWORDClientException | SWORDError | ProtocolViolationException e) {
			logger.error("Exception by making deposit: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		} 
	}
	
	
	//=== PublicationRepository interface methods ===
	
	
	/**
	 * {@inheritDoc}
	 * <p> 
	 * In DSpace SWORD-v2 protocol will be used.
	 *  	
	 * @param userLogin
	 * @param collectionURL
	 * @param fileFullPath
	 * @return
	 */
	@Override
	public boolean publishFile(String userLogin, String collectionURL, File fileFullPath) {
		
		String mimeFormat = "application/zip"; // for every file type, to publish even "XML" files as a normal file
		String packageFormat = getPackageFormat(fileFullPath.getName()); //zip-archive or separate file
		
		if(publishElement(userLogin, collectionURL, mimeFormat, packageFormat, fileFullPath, null) != null) {
			return true;
		} else {
			return false;
		}
	}
		
	
	/**
	 * {@inheritDoc}
	 * Publish metadata as a Map.
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param metadataMap
	 * @return
	 */
	@Override
	public boolean publishMetadata(String userLogin, String collectionURL, Map<String, String> metadataMap) {
		
		String mimeFormat = "application/atom+xml";
		String packageFormat = UriRegistry.PACKAGE_BINARY;
		
		if(publishElement(userLogin, collectionURL, mimeFormat, packageFormat, null, metadataMap) != null) {
			return true;
		} else {
			return false;
		}		
	}
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Publish metadata as a XML-file in ATOM-format.
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param metadataFileXML - file in XML-format (ATOM format of the metadata description) and with an XML-extension
	 * @return 
	 */
	@Override
	public boolean publishMetadata(String userLogin, String collectionURL, File metadataFileXML) {
		
		// Check if file has an XML-extension
		if(!getFileExtension(metadataFileXML.getName()).toLowerCase().equals("xml")) {
			return false;
		}
		
		String mimeFormat = "application/atom+xml";
		String packageFormat = getPackageFormat(metadataFileXML.getName());
		
		return (publishElement(userLogin, collectionURL, mimeFormat, packageFormat, metadataFileXML, null) != null);
	}
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * In DSpace SWORD-v2 protocol will be used.
	 */
	@Override
	public boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, Map<String, String> metadataMap) {
		
		String mimeFormat = "application/zip"; //as a common file (even for XML)
		String packageFormat = getPackageFormat(fileFullPath.getName());
		
		// Step 1: publish file (as file or archive), without metadata
		String editLink = publishElement(userLogin, collectionURL, mimeFormat, packageFormat, fileFullPath, null);
		
		// Step 2: add metadata (as a Map structure)
		if (editLink != null) {
			return publishMetadata(userLogin, editLink, metadataMap);
		} else {
			return false;
		}
		
		//If replace order (step 1: metadata, step 2: file) --> Bad request, ERROR 400		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * In DSpace SWORD-v2 protocol will be used.
	 */
	@Override
	public boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, File metadataFileXML) {
		
		// Check if metadata file has an XML-extension
		if (!getFileExtension(metadataFileXML.getName()).toLowerCase().equals("xml")) {
			return false;
		}
		
		String mimeFormat = "application/zip"; //as a common file (even for XML)
		String packageFormat = getPackageFormat(fileFullPath.getName());
		
		// Step 1: publish file (as file or archive), without metadata
		String editLink = publishElement(userLogin, collectionURL, mimeFormat, packageFormat, fileFullPath, null); 
		
		// Step 2: add metadata (as XML-file)
		if (editLink != null) {
			return publishMetadata(userLogin, editLink, metadataFileXML); 
		} else {
			return false;
		}
		
		//If replace order (step 1: metadata, step 2: file) --> Bad request, ERROR 400
	}
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented yet!
	 */
	@Override
	public Repository getDAO() {
		return dao;
	}

		
	/**
	 * {@inheritDoc}
	 * <p>
	 * For DSpace it is done by access to the Service Document via SWORD-protocol.
	 * 
	 * @return {@code true} if service document is accessible, and {@code false} if not (e.g. by Error 403).  
	 */
	@Override
	public boolean isAccessible() {
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd);
		
		if(this.getServiceDocument(swordClient, sword_servicedocument, authCredentials) != null) {
			return true;
		} else {
			return false;
		}		
	}
			
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * In DSpace it will be checked via access to the service document (SWORD-protocol)
	 */
	@Override
	public boolean isUserRegistered(String loginName) {	
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd, loginName);// "on-behalf-of: loginName"
		
		if(this.getServiceDocument(swordClient, sword_servicedocument, authCredentials) != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * In DSpace it will be checked via access to the service document (SWORD-protocol)
	 */
	@Override
	public boolean isUserAssigned(String loginName) {
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd, loginName); //"on-behalf-of: loginName"
		ServiceDocument serviceDocument = this.getServiceDocument(swordClient, sword_servicedocument, authCredentials);

		int collectionCount = 0;
		for(SWORDWorkspace workspace : serviceDocument.getWorkspaces()) {
			collectionCount += workspace.getCollections().size(); //increment collection count
		}
		
		return (collectionCount > 0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public Map<String, String> getUserAvailableCollectionsWithTitle(String loginName) {
		
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd, loginName); // "on-behalf-of: loginName"		
		return this.getAvailableCollectionsViaSWORD(authCredentials);
	}

	/**
	 * {@inheritDoc}
	 */
	
	public Map<String, String> getSaraAvailableCollectionsWithTitle() {
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd); // login as "saraUser"		
		return this.getAvailableCollectionsViaSWORD(authCredentials);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented yet!
	 */
	@Override
	public String getCollectionName(String uuid) {
		// TODO implement it using REST
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented yet!
	 */
	@Override
	public String getMetadataName(String uuid) {
		// TODO implement it using REST
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented yet!
	 */
	@Override
	public boolean publishItem(Item item) {
		// TODO implement it using SWORD
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented yet!
	 */
	@Override
	public void dump() {
		// TODO Auto-generated method stub	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getAvailableCollectionPaths(String separator, String loginName) {
		AuthCredentials authCredentials;
		if (loginName == null || loginName.equals(sword_user)) {
			authCredentials = new AuthCredentials(sword_user, sword_pwd); // as service user 
		} else {
			authCredentials = new AuthCredentials(sword_user, sword_pwd, loginName); // on-behalf-of		
		}
		
		Map<String, String> collectionsMap = getAvailableCollectionsViaSWORD(authCredentials);
		
		for(String url: collectionsMap.keySet()) {
			List<String> communities = getCommunityListForCollection(url);
			String fullName = "";
			for(String community: communities) {
				fullName += community + separator; // add community + separator
			}
			fullName += collectionsMap.get(url); // add title
			collectionsMap.put(url, fullName);
		}		
		return collectionsMap;		
	}
	
	@Override
	public Hierarchy getHierarchy(String loginName) {
		Hierarchy hierarchy;
		
		hierarchy = new Hierarchy("");
		hierarchy.setName("Uni-Bibliographie");
		
		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd, loginName); // "on-behalf-of: loginName"		
		Map<String, String> collectionsMap = this.getAvailableCollectionsViaSWORD(authCredentials);
		
		for(String url: collectionsMap.keySet()) {
			List<String> communities = getCommunityListForCollection(url);
			Hierarchy entry = hierarchy;
			for(String community: communities) {
				boolean found = false;
				for (Hierarchy child: entry.getChildren()) {
					if (child.getName().equals(community)) {
						entry = child;
						found = true;
						break;
					}
				}
				if (!found) entry = entry.addChild(community);
			}
			entry = entry.addChild(collectionsMap.get(url));
			entry.setURL(url);
			entry.setCollection(true);
		}		
		
		return hierarchy;
	}
	
}

