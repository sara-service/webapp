/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.repositories;

import bwfdm.sara.metadata.MetadataCollection;
import javax.ws.rs.core.Cookie;

/**
 * Interface for the typical DSpace Publication Repository.
 * 
 * Extends PublicationRepository-Intrface
 * 
 * @author vk
 */
public interface DSpaceREST {
    
    public boolean isRestEnable();
    public boolean login(String email, String password);
    public boolean logout();
    public boolean isAuthenticated();
    public String getConnectionStatus();
    //public String getToken();
    //public Cookie getCookie();
    
    public String createCommunity(String communityName, String parentCommunityID);
    public String deleteCommunity(String communityID);
    public String updateCommunity(String communityName, String communityID);
    
    public String getObjectProperties(String objectName, String objectID);
    
    public String createCollection(String collectionName, String parentCommunityID);
    public String deleteCollection(String collectionID);
    public String updateCollection(String collectionName, String collectionID);
    
    public String createItem(String itemName, String itemTitle, String collectionID);
    public boolean deleteItem(String itemID);
    public boolean deleteItemInCollection(String collectionID, String itemID);
    
    public String itemAddMetadata(String itemID, String metadata);
    public boolean itemUpdateMetadata(String itemID, String metadataEntry);
    public String itemAddBitstream(String itemID, String bitstreamToAdd);
    public boolean itemDeleteBitstream(String itemID, String bitstreamToDelete);
    
    public boolean downloadBitstream(String bitstreamID, String filenameToSave);
    
    // Not important methods
    public String getAllCommunities();
    public String getCommunityById(String id);
    public String getAllCollections();
    public String getCollectionById(String id);
    public String getAllItems();
    public String getItemById(String id);
    public String getItemMetadataById(String id);
    
    
    

}
