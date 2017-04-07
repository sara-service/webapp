/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication.dspace;

//import bwfdm.sara.metadata.MetadataCollection;
import javax.ws.rs.core.Cookie;

/**
 * Interface for the typical DSpace Publication Repository.
 * 
 * Extends PublicationRepository-Intrface
 * 
 * @author vk
 */
public interface DSpaceRest {
    
    public boolean isRestEnable();
    public boolean login(String email, String password);
    public boolean logout();
    public boolean isAuthenticated();
    public String getConnectionStatus();
    //public String getToken();
    //public Cookie getCookie();
    
    /* Community */
    public String createCommunity(String communityName, String parentCommunityID);
    public String deleteCommunity(String communityID);
    public String updateCommunity(String communityName, String communityID);
    public String getAllCommunities(); // not important
    public String getCommunityById(String id); // not important
   
    public String getObjectProperties(String objectName, String objectID);
    
    
    /* Collection */
    public String createCollection(String collectionName, String parentCommunityID);
    public String deleteCollection(String collectionID);
    public String updateCollection(String collectionName, String collectionID);
    public String getAllCollections(); // not important
    public String getCollectionById(String id); // not important
    
    
    /* Item */
    public String createItem(String itemName, String itemTitle, String collectionID);
    public String deleteItem(String itemID);
    public String addItemMetadata(String itemID, String metadata);
    public String updateItemMetadata(String itemID, String metadata);
    public String clearItemMetadata(String itemID);
    public String getItemBitstreams(String itemID);
    public String addItemBitstream(String itemID, String bitstreamDescription);
    public String deleteItemBitstream(String itemID, String bitstreamToDelete);
    public String getAllItems(); // not important
    public String getItemById(String id); // not important
    public String getItemMetadataById(String id); // not important
    
    
    /* Bitstreams */
    public String getAllBitstreams();
    public String downloadBitstream(String bitstreamID, String filenameToSave);
    

}
