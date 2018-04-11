package bwfdm.sara.publication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * General Interface for the publication repository.
 * 
 * @author sk, vk
 */

import java.util.Map;

public interface PublicationRepository {

	/**
	 * Check if publication repository is accessible via API
	 * 
	 * @return
	 */
	public boolean isAccessible();
	
	/**
	 * Check if user is registered in the publication repository
	 * 
	 * @param loginName
	 * @return
	 */
	public boolean isUserRegistered(String loginName);
	
	/**
	 * Check if user is assigned to publish something in the repository
	 *
	 * @param loginName
	 * @return {@code true} if count of user available collections is great than zero, 
	 * 		   otherwise {@code false} 
 	 */
	public boolean isUserAssigned(String loginName);
	
	
	/**
	 * Get collections which are available for the user
	 * 
	 * @param loginName - login name of user, if 'null' service user is taken
	 * @return hierarchy tree containing leafs (collections) and branches (communities)
	 */
	public Hierarchy getHierarchy(String loginName);
	
	
	/**
	 * Get collections, which are available for the user
	 * Could be, that user has an access only for some specific collections.
	 *  
	 * @param loginName
	 * @return Map of Strings, where key="Collection full URL", value="Collection title"
	 */
	public Map<String, String> getUserAvailableCollectionsWithTitle(String loginName);
	
	/**
	 * Get collections, which are available for the user, and show their full name
	 * (e.g. for DSpace-repository it means "community/subcommunity/collection")
	 * <p>
	 * Could be, that user has an access only for some specific collections.
	 *  
	 * @param loginName, nameSeparator
	 * @return Map of Strings, where key="Collection full URL", value="Collection full name"
	 */
	public Map<String, String> getUserAvailableCollectionsWithFullName(String loginName, String fullNameSeparator);
	
	/**
	 * Get available for the SARA-Service collections for publication.
	 * As credentials for the request are used login/password of the SARA-user
	 * 
	 * @return Map of Strings, where key="Collection full URL", value="Collection title" 
	 */
	public Map<String, String> getSaraAvailableCollectionsWithTitle();
	
	/**
	 * Get available for the SARA-service collections with full name
	 * (e.g. for DSpace-repository it means "community/subcommunity/collection")
	 *  
	 * @param fullNameSeparator 
	 * @return Map of Strings, where key="Collection full URL", value="Collection full name" 
	 */
	public Map<String, String> getSaraAvailableCollectionsWithFullName(String fullNameSeparator);
	
	/**
	 * Publish a file to some collections, which is available for the user.
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param fileFullPath
	 * @return
	 */
	public boolean publishFile(String userLogin, String collectionURL, File fileFullPath);
	
	/**
	 * Publish metada only (without any file) to some collection, which is available for the user.
	 * Metadata are described as a {@link java.util.Map}. 
	 *  
	 * @param userLogin
	 * @param collectionURL
	 * @param metadataMap
	 * @return
	 */
	public boolean publishMetadata(String userLogin, String collectionURL, Map<String, String> metadataMap);
		
	/**
	 * Publish metada only (without any file) to some collection, which is available for the user.
	 * Metadata are described in the xml-file.
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param metadataFileXML
	 * @return
	 */
	public boolean publishMetadata(String userLogin, String collectionURL, File metadataFileXML);
	
	/**
	 * Publish a file together with the metadata.
	 * Metadata are described as a {@link java.util.Map}. 
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param fileFullPath
	 * @param metadataMap
	 * @return
	 */
	public boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, Map<String, String> metadataMap);
	
	/**
	 * Publish a file together with the metadata.
	 * Metadata are described in the xml-file.
	 * 
	 * @param userLogin
	 * @param collectionURL
	 * @param fileFullPath
	 * @param metadataFileXML
	 * @return
	 */
	public boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, File metadataFileXML);

	
	public Repository getDAO();

	public String getCollectionName(String uuid);

	public String getMetadataName(String uuid);

	public boolean publishItem(Item item);

	public void dump();
}
