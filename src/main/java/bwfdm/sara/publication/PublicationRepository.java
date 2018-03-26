package bwfdm.sara.publication;

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
	public Boolean isAccessible();

		
	/**
	 * Set login and password of the user.
	 * Password is needed for the communication with the publication repository via API (e.g. SWORD or REST)
	 * <p>
	 * If the publication repository is DSpace version 6.x or bellow, you should put login/password ONLY of SARA-user.
	 * Credentials of the SARA-user will be used for the REST/SWORD mechanism. 
	 * This mechanism is needed because of limitations of DSpace-API, where password is always needed.   
	 * <p>
	 *   
	 * @param user
	 * @param password
	 */
	public void setCredentials(String user, String password);
	
	
	/**
	 * Check if user is registered in the publication repository
	 * 
	 * @param loginName
	 * @return
	 */
	public Boolean isUserRegistered(String loginName);
	
	
	/**
	 * Check if user is assigned to publish something in the repository
	 *
	 * @param loginName
	 * @return {@code true} if count of user available collections is great than zero, 
	 * 		   otherwise {@code false} 
 	 */
	public Boolean isUserAssigned(String loginName);
	
	
	/**
	 * Get collections, which are available for the user
	 * Could be, that user has an access only for some specific collections.
	 *  
	 * @param loginName
	 * @return Map of Strings, where key="Collection full URL", value="Collection title"
	 */
	public Map<String, String> getUserAvailableCollectionTitles(String loginName);
	
	
	/**
	 * Get collections, which are available for the user, and show their full name
	 * (e.g. for DSpace-repository it means "community/subcommunity/collection")
	 * <p>
	 * Could be, that user has an access only for some specific collections.
	 *  
	 * @param loginName
	 * @return Map of Strings, where key="Collection full URL", value="Collection full name"
	 */
	public Map<String, String> getUserAvailableCollectionFullNames(String loginName);
	
	
	/**
	 * Get available for the SARA-Service collections for publication.
	 * As credentials for the request are used login/password of the SARA-user
	 * 
	 * @return Map of Strings, where key="Collection full URL", value="Collection title" 
	 */
	public Map<String, String> getAvailableCollectionTitles();
	
	
	/**
	 * Get available for the SARA-service collections with full name
	 * (e.g. for DSpace-repository it means "community/subcommunity/collection")
	 *  
	 * @return Map of Strings, where key="Collection full URL", value="Collection full name" 
	 */
	public Map<String, String> getAvailableCollectionFullNames();
	
	
	/**
	 * Get title of the collection, based on the URL
	 * 
	 * @param URL
	 * @return
	 */
	public String getCollectionTitleByURL(String URL);
	
	
	/**
	 * Get full name of the collection based on the URL
	 * If publication repository is DSpace, 
	 * then full name has to include "community/subcommunity/collection-title"
	 * 
	 * @param URL
	 * @return
	 */
	public String getCollectionFullNameByURL(String URL);
	
	
	//TODO: remove it
	/**
	 * Get URL of the collection with the title.
	 *  
	 * @param collectionTitle
	 * @return URL as a String
	 */
	public String getCollectionURL(String collectionTitle);
	
	
	
	
	
	
	
	// Old methods
	
	public Repository getDAO();

	public String getCollectionName(String uuid);

	public String getMetadataName(String uuid);

	public Boolean publishItem(Item item);

	public void dump();
}
