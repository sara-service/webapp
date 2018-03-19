package bwfdm.sara.publication.dspace;

import java.util.Map;

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

public class DSpace_v6 implements PublicationRepository{

	private static final Logger logger = LoggerFactory.getLogger(DSpace_v6.class);
	
	private String saraUser;
	private String saraPassword;
		
	private String serviceDocumentURL;
	
	
	public DSpace_v6(String serviceDocumentURL, String saraUser, String saraPassword) {
		
		this.saraUser = saraUser;
		this.saraPassword = saraPassword;
		this.serviceDocumentURL = serviceDocumentURL;
	}
	
	public void setSaraUser(String saraUser) {
		this.saraUser = saraUser;
	}
	
	public void setSaraPassword(String saraPassword) {
		this.saraPassword = saraPassword;
	}
	
	public void setServiceDocumentURL(String serviceDocumentURL) {
		this.serviceDocumentURL = serviceDocumentURL;
	}
	
	
	
	private ServiceDocument getServiceDocument(SWORDClient swordClient, String serviceDocumentURL, AuthCredentials authCredentials) {
		ServiceDocument serviceDocument = null;
		try {
			serviceDocument = swordClient.getServiceDocument(this.serviceDocumentURL, authCredentials);
		
		} catch (SWORDClientException | ProtocolViolationException e) {
			logger.error("Exception by accessing service document: " 
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
		return serviceDocument;
	}
	
	
		
	
	@Override
	public Repository getDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	/**
	 * Check if repository is accessible for SARA-Server
	 */
	@Override
	public Boolean isAccessible() {
		
		SWORDClient swordClient = new SWORDClient();
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword);
		
		return ((this.getServiceDocument(swordClient, serviceDocumentURL, authCredentials)!= null) ? true : false);		
	}
	
		
	
	/**
	 * Check if the user (loginName) is registered in the repository
	 */
	@Override
	public Boolean isUserRegistered(String loginName) {
		
		SWORDClient swordClient = new SWORDClient();
		
		// Authentification with "on-behalf-of: loginName"
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword, loginName);
		
		return ((this.getServiceDocument(swordClient, serviceDocumentURL, authCredentials) != null) ? true : false);
	}

	
	/**
	 * Check if the user is assigned to make a publication in the repository 
	 * (if there are some available for the user collections)
	 */
	@Override
	public Boolean isUserAssigned(String loginName) {
		
		SWORDClient swordClient = new SWORDClient();
		
		// Authentification with "on-behalf-of: loginName"
		AuthCredentials authCredentials = new AuthCredentials(saraUser, saraPassword, loginName);
		ServiceDocument serviceDocument = this.getServiceDocument(swordClient, serviceDocumentURL, authCredentials);
		int collectionCount = 0;

// Correct and optimal
//		for(SWORDWorkspace workspace : serviceDocument.getWorkspaces()) {
//			collectionCount += workspace.getCollections().size();
//		}
//		System.out.println("collectionCount = " + collectionCount);

		
		// For testing
		// FIXME: replace it with the solution above!
		for(SWORDWorkspace workspace : serviceDocument.getWorkspaces()) {
			System.out.println("Workspace: " + workspace.getTitle());
			for (SWORDCollection collection : workspace.getCollections()) {
				System.out.println("-- Collection: " + collection.getTitle());
				collectionCount++;
			}
		}
			
		return ((collectionCount > 0) ? true : false);
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
	public Map<String, String> getAvailableCollections() {
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

}
