package bwfdm.sara.publication.dspace;

import java.util.ArrayList;
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
import bwfdm.sara.utils.WebUtils;

public class DSpace_v6 implements PublicationRepository{

	protected static final Logger logger = LoggerFactory.getLogger(DSpace_v6.class);
	
	// For SWORD
	protected final String saraUser;
	protected final String saraPassword;
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
	 * Get collections via SWORD v2
	 * 
	 * @return Map<String, String> where key=URL, value=Title
	 */
	private Map<String, String> getCollectionsSWORD(AuthCredentials authCredentials){
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
	 * @return
	 */
	public List<String> getCommunitiesForCollection(String collectionURL){

		final Invocation.Builder invocationBuilder = communitiesWebTarget.request();
		invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
		invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
		final Response response = invocationBuilder.get();

		System.out.println(WebUtils.readResponseEntity(String.class, response));
		//WebUtils.readResponseEntity(String.class, response);
		
		return null;
		
		
	}
	

	public String convertHandleToURL(String URL) {
		return null;
	}
	
	public String convertURLToHandle(String handle) {
		return null;
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

	
	/**
	 * {@inheritDoc}
	 */
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
	public Map<String, String> getUserAvailableCollectionTitles(String loginName) {
		
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword, loginName); // "on-behalf-of: loginName"		
		return this.getCollectionsSWORD(authCredentials);
	}

	
	@Override
	public Map<String, String> getAvailableCollectionTitles() {
		
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword); // login as "saraUser"		
		return this.getCollectionsSWORD(authCredentials);
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
		
	}


	@Override
	public String getCollectionURL(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, String> getUserAvailableCollectionFullNames(String loginName) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, String> getAvailableCollectionFullNames() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getCollectionTitle(String URL) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getCollectionFullNameByURL(String URL) {
		// TODO Auto-generated method stub
		return null;
	}

}
