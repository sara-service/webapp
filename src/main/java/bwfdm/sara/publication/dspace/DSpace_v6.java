package bwfdm.sara.publication.dspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.ProtocolViolationException;
import org.swordapp.client.SWORDClient;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDCollection;
import org.swordapp.client.SWORDWorkspace;
import org.swordapp.client.ServiceDocument;

import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.dspace.dto.v6.CollectionObject;
import bwfdm.sara.publication.dspace.dto.v6.HierarchyObject;
import bwfdm.sara.utils.JsonUtils;
import bwfdm.sara.utils.WebUtils;

public class DSpace_v6 implements PublicationRepository{

	protected static final Logger logger = LoggerFactory.getLogger(DSpace_v6.class);
	
	// For SWORD
	protected String saraUser;
	protected String saraPassword;
	protected final String urlServiceDocument;
		
	// For REST
	//TODO: remove, what we do not need
	protected final String urlREST;
	protected final WebTarget restWebTarget;
	protected final WebTarget loginWebTarget;
	protected final WebTarget logoutWebTarget;
	protected final WebTarget testWebTarget;
	protected final WebTarget statusWebTarget;
	protected final WebTarget communitiesWebTarget;
	protected final WebTarget collectionsWebTarget;
	protected final WebTarget itemsWebTarget;
	protected final WebTarget bitstreamsWebTarget;
	protected final WebTarget handleWebTarget;
	protected final WebTarget hierarchyWebTarget;
	protected String token;
	protected Cookie cookie;
	protected Client client;
	
	
	public DSpace_v6(String urlServiceDocument, String urlREST, String saraUser, String saraPassword) {
		this.saraUser = saraUser;
		this.saraPassword = saraPassword;
		this.urlREST = urlREST;
		this.urlServiceDocument = urlServiceDocument;
		
		//client = ClientBuilder.newClient();
		client = WebUtils.getClientWithoutSSL(); // Ignore SSL-Verification
		
		// WebTargets
		restWebTarget = client.target(this.urlREST);
		loginWebTarget = restWebTarget.path("login");
		logoutWebTarget = restWebTarget.path("logout");
		testWebTarget = restWebTarget.path("test");
		statusWebTarget = restWebTarget.path("status");
		communitiesWebTarget = restWebTarget.path("communities");
		collectionsWebTarget = restWebTarget.path("collections");
		itemsWebTarget = restWebTarget.path("items");
		bitstreamsWebTarget = restWebTarget.path("bitstreams");
		handleWebTarget = restWebTarget.path("handle");
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
			serviceDocument = swordClient.getServiceDocument(this.urlServiceDocument, authCredentials);
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
		ServiceDocument serviceDocument = this.getServiceDocument(swordClient, urlServiceDocument, authCredentials);
		
		for(SWORDWorkspace workspace : serviceDocument.getWorkspaces()) {
			for (SWORDCollection collection : workspace.getCollections()) {
				// key = full URL, value = Title
				collections.put(collection.getHref().toString(), collection.getTitle());
			}
		}
		return collections;
	}
	
	
	/**
	 * Get a list of communities for the collection
	 * Specific only for DSpace-6.
	 * <p>
	 * REST and SWORD requests are used.
	 * 
	 * @return a {@code List<String>} of communities (0 or more communities are possible) 
	 * 		   or {@code null} if a collection was not found
	 */
	public List<String> getCommunitiesForCollection(String collectionURL){
		
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
		
		String swordCollectionPath = ""; //collectionURL without a hostname and port
		
		// Find a collectionURL inside of all avaialble collections. SWORD is used.
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword);
		ServiceDocument serviceDocument = this.getServiceDocument(swordClient, urlServiceDocument, authCredentials);
		
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
	
	
	/*
	 * -------- Interface functions
	 */
	
	
	@Override
	public Repository getDAO() {
		// TODO Auto-generated method stub
		return null;
	}

		
	/**
	 * {@inheritDoc}
	 * <p>
	 * For DSpace it is done by access to the Service Document via SWORD-protocol.
	 * 
	 * @return {@code true} if service document is accessible, and {@code false} if not (e.g. by Error 403).  
	 */
	@Override
	public Boolean isAccessible() {
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword);
		
		if(this.getServiceDocument(swordClient, urlServiceDocument, authCredentials) != null) {
			return true;
		} else {
			return false;
		}		
	}
			
	
	/**
	 * {@inheritDoc}
	 * In DSpace it will be checked via access to the service document (SWORD-protocol)
	 */
	@Override
	public Boolean isUserRegistered(String loginName) {	
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword, loginName);// "on-behalf-of: loginName"
		
		if(this.getServiceDocument(swordClient, urlServiceDocument, authCredentials) != null) {
			return true;
		} else {
			return false;
		}
	}

	
	@Override
	public Boolean isUserAssigned(String loginName) {
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword, loginName); //"on-behalf-of: loginName"
		ServiceDocument serviceDocument = this.getServiceDocument(swordClient, urlServiceDocument, authCredentials);

		int collectionCount = 0;
		for(SWORDWorkspace workspace : serviceDocument.getWorkspaces()) {
			collectionCount += workspace.getCollections().size(); //increment collection count
		}
		
		return ((collectionCount > 0) ? true : false);
	}
	
	
	@Override
	public Map<String, String> getUserAvailableCollectionsWithTitle(String loginName) {
		
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword, loginName); // "on-behalf-of: loginName"		
		return this.getAvailableCollectionsViaSWORD(authCredentials);
	}

	
	@Override
	public Map<String, String> getSaraAvailableCollectionsWithTitle() {
		
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword); // login as "saraUser"		
		return this.getAvailableCollectionsViaSWORD(authCredentials);
	}
	
	
	@Override
	public String getCollectionName(String uuid) {
		return null;
	}

	@Override
	public String getMetadataName(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	@Override
	public Boolean publishItem(Item item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dump() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCredentials(String user, String password) {
		// TODO Auto-generated method stub
		this.saraUser = user;
		this.saraPassword = password;
	}


	@Override
	public String getCollectionURL(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * {@inheritDoc}	 
	 * */
	@Override
	public Map<String, String> getUserAvailableCollectionsWithFullName(String loginName, String fullNameSeparator) {
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword, loginName); // "on-behalf-of: loginName"		
		Map<String, String> collectionsMap = this.getAvailableCollectionsViaSWORD(authCredentials);
		
		for(String url: collectionsMap.keySet()) {
			List<String> communities = this.getCommunitiesForCollection(url);
			String fullName = "";
			for(String community: communities) {
				fullName += community + fullNameSeparator; // add community + separator
			}
			fullName += collectionsMap.get(url); // add title
			collectionsMap.put(url, fullName);
		}		
		return collectionsMap;
	}


	@Override
	public Map<String, String> getSaraAvailableCollectionsWithFullName(String separator) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getCollectionTitleByURL(String URL) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getCollectionFullNameByURL(String URL) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
