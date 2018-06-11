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

public class DSpace_v6 implements PublicationRepository {
	protected static final Logger logger = LoggerFactory.getLogger(DSpace_v6.class);

	private final String rest_api_endpoint;
	private final String sword_user, sword_pwd, sword_api_endpoint,
			sword_servicedocumentpath;
	private final Repository dao;

	// for SWORD
	private AuthCredentials sword_authcredentials;
	private ServiceDocument sword_servicedocument;
	private SWORDClient sword_client;
	private List<SWORDWorkspace> sword_workspaces;

	// For REST
	private final WebTarget restWebTarget;
	private final WebTarget collectionsWebTarget;
	private CollectionObject[] collections;
	private final WebTarget hierarchyWebTarget;
	private HierarchyObject rest_hierarchy;

	private Client rest_client;

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
		sword_servicedocumentpath = sword_api_endpoint + "/servicedocument";

		rest_client = WebUtils.getClientWithoutSSL(); // Ignore SSL-Verification
		// client = ClientBuilder.newClient();

		// WebTargets
		restWebTarget = rest_client.target(rest_api_endpoint);
		collectionsWebTarget = restWebTarget.path("collections");
		hierarchyWebTarget = restWebTarget.path("hierarchy");
		rest_hierarchy = null;

		sword_client = new SWORDClient();
		sword_authcredentials = new AuthCredentials(sword_user, sword_pwd);
		sword_servicedocument = null;
		sword_workspaces = null;
	}

	public DSpace_v6(String serviceDocumentURL, String restURL, String saraUser, String saraPassword) {
		sword_servicedocumentpath = serviceDocumentURL;
		sword_user = saraUser;
		sword_pwd = saraPassword;
		sword_api_endpoint = null;
		rest_api_endpoint = restURL;
		dao = null;

		rest_client = WebUtils.getClientWithoutSSL(); // Ignore SSL-Verification
		// client = ClientBuilder.newClient();

		restWebTarget = rest_client.target(rest_api_endpoint);
		collectionsWebTarget = restWebTarget.path("collections");
		collections = null;
		hierarchyWebTarget = restWebTarget.path("hierarchy");

		sword_client = new SWORDClient();
		sword_authcredentials = new AuthCredentials(sword_user, sword_pwd);
		sword_servicedocument = null;
	}

	private ServiceDocument serviceDocument(AuthCredentials authCredentials) {
		try {
			sword_servicedocument = sword_client.getServiceDocument(
					sword_servicedocumentpath, authCredentials);
			if (sword_servicedocument != null)
				sword_workspaces = sword_servicedocument.getWorkspaces();
		} catch (SWORDClientException | ProtocolViolationException e) {
			logger.error("Exception by accessing service document: "
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
		return sword_servicedocument;
	}

	private ServiceDocument serviceDocument() {
		if (sword_servicedocument != null) {
			return sword_servicedocument;
		} else {
			return serviceDocument(sword_authcredentials);
		}
	}

	@Override
	public boolean isAccessible() {
		return (serviceDocument() != null);
	}

	@Override
	public boolean isUserRegistered(String loginName) {
		sword_authcredentials = new AuthCredentials(sword_user, sword_pwd,
				loginName);
		return (serviceDocument() != null);
	}

	@Override
	public boolean isUserAssigned(String loginName) {
		final ServiceDocument sd = serviceDocument();

		if (sd == null)
			return false;

		int collectionCount = 0;
		for (SWORDWorkspace workspace : sword_workspaces) {
			collectionCount += workspace.getCollections().size(); // increment
																	// collection
																	// count
		}

		return (collectionCount > 0);
	}

	@Override
	public Hierarchy getHierarchy(String loginName) {
		Hierarchy hierarchy;

		hierarchy = new Hierarchy("");
		hierarchy.setName("Uni-Bibliographie");

		AuthCredentials authCredentials = new AuthCredentials(sword_user,
				sword_pwd, loginName); // "on-behalf-of:
										// loginName"
		Map<String, String> collectionsMap = getAvailableCollectionsViaSWORD(
				authCredentials);

		for (String url : collectionsMap.keySet()) {
			List<String> communities = getCommunityListForCollection(url);
			Hierarchy entry = hierarchy;
			for (String community : communities) {
				boolean found = false;
				for (Hierarchy child : entry.getChildren()) {
					if (child.getName().equals(community)) {
						entry = child;
						found = true;
						break;
					}
				}
				if (!found)
					entry = entry.addChild(community);
			}
			entry = entry.addChild(collectionsMap.get(url));
			entry.setURL(url);
			entry.setCollection(true);
		}

		return hierarchy;
	}

	private List<String> getCommunityListForCollection(String collectionURL) {
		String collectionHandle = getCollectionHandle(collectionURL);
		if (collectionHandle == null) {
			return null;
		}

		List<String> communityList = new ArrayList<String>(0);
		
		if (rest_hierarchy == null) {
			final Response response = getResponse(hierarchyWebTarget,
					MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
			rest_hierarchy = JsonUtils.jsonStringToObject(
					WebUtils.readResponseEntity(String.class, response),
					HierarchyObject.class);
		}

		// Get List of communities or "null", if collection is not found
		communityList = rest_hierarchy.getCommunityListForCollection(
				rest_hierarchy, collectionHandle, communityList);

		if (communityList != null) {
			communityList.remove(0); // remove "Workspace" - it is not a
										// community,
										// but it is in the first level of the
										// hierarchy
		}
		return communityList; // List of communities ( >= 0) or "null"
	}

	public String getCollectionHandle(String collectionURL) {
		logger.info("collectionsWebTarget FAST");

		String swordCollectionPath = ""; // collectionURL without a host name
											// and port

		// Find a collectionURL inside of all available collections. SWORD is used.
		for (SWORDWorkspace workspace : sword_workspaces) {
			for (SWORDCollection collection : workspace.getCollections()) {
				if (collection.getHref().toString().equals(collectionURL)) {
					swordCollectionPath = collection.getHref().getPath();
				}
			}
		}

		// Get all collections via REST to check, if swordCollectionPath
		// contains a
		// REST-handle
		if (collections == null) {
			final Response response = getResponse(collectionsWebTarget,
					MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
			collections = JsonUtils.jsonStringToObject(
					WebUtils.readResponseEntity(String.class, response),
					CollectionObject[].class);
		}

		// Compare REST-handle and swordCollectionPath
		for (CollectionObject collection : collections) {
			if (swordCollectionPath.contains(collection.handle)) {
				return collection.handle;
			}
		}
		return null; // collectionURL was not found
	}

	private Response getResponse(WebTarget webTarget, String contentType,
			String acceptType) {
		final Invocation.Builder invocationBuilder = webTarget.request();
		invocationBuilder.header("Content-Type", contentType);
		invocationBuilder.header("Accept", acceptType);
		final Response response = invocationBuilder.get();
		return response;
	}

	private Map<String, String> getAvailableCollectionsViaSWORD(
			AuthCredentials authCredentials) {
		Map<String, String> collections = new HashMap<String, String>();

		for (SWORDWorkspace workspace : sword_workspaces) {
			for (SWORDCollection collection : workspace.getCollections()) {
				// key = full URL, value = Title
				collections.put(collection.getHref().toString(),
						collection.getTitle());
			}
		}
		return collections;
	}

	@Override
	public Map<String, String> getAvailableCollectionPaths(String separator,
			String loginName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean publishFile(String userLogin, String collectionURL,
			File fileFullPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean publishMetadata(String userLogin, String collectionURL,
			Map<String, String> metadataMap) {

		String mimeFormat = "application/atom+xml";
		String packageFormat = UriRegistry.PACKAGE_BINARY;

		if (publishElement(userLogin, collectionURL, mimeFormat, packageFormat,
				null, metadataMap) != null) {
			return true;
		} else {
			return false;
		}
	}

	private String publishElement(String userLogin, String collectionURL,
			String mimeFormat, String packageFormat, File file,
			Map<String, String> metadataMap) {

		// Check if only 1 parameter is used (metadata OR file).
		// Multipart is not supported.
		if (((file != null) && (metadataMap != null))
				|| ((file == null) && (metadataMap == null))) {
			return null;
		}

		AuthCredentials authCredentials = new AuthCredentials(sword_user, sword_pwd, userLogin);

		Deposit deposit = new Deposit();

		try {
			// Check if "meta data as a Map"
			if (metadataMap != null) {
				EntryPart ep = new EntryPart();
				for (Map.Entry<String, String> metadataEntry : metadataMap
						.entrySet()) {
					ep.addDublinCore(metadataEntry.getKey(), metadataEntry.getValue());
				}
				deposit.setEntryPart(ep);
			}

			// Check if "file"
			if (file != null) {
				deposit.setFile(new FileInputStream(file));
				deposit.setFilename(file.getName()); // deposit works properly
														// ONLY with a
														// "filename" parameter
														// --> in curl: -H "Content-Disposition: filename=file.zip"
			}

			deposit.setMimeType(mimeFormat);
			deposit.setPackaging(packageFormat);
			deposit.setInProgress(true);
			// deposit.setMd5("fileMD5");
			// deposit.setSuggestedIdentifier("abcdefg");

			DepositReceipt receipt = sword_client.deposit(collectionURL,
					deposit, authCredentials);
			return receipt.getLocation(); // "Location" parameter from the response

		} catch (FileNotFoundException e) {
			logger.error("Exception by accessing a file: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;

		} catch (SWORDClientException | SWORDError | ProtocolViolationException e) {
			logger.error("Exception by making deposit: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
	}

	@Override
	public boolean publishMetadata(String userLogin, String collectionURL, File metadataFileXML) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean publishFileAndMetadata(String userLogin,
			String collectionURL, File fileFullPath,
			Map<String, String> metadataMap) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean publishFileAndMetadata(String userLogin,
			String collectionURL, File fileFullPath, File metadataFileXML) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Repository getDAO() {
		return dao;
	}

	@Override
	public String getCollectionName(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMetadataName(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean publishItem(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dump() {
		// TODO Auto-generated method stub
	}

}