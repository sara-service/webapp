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
    
    /* Community */
    public String createCommunity(String communityName, String parentCommunityID);
    public String deleteCommunity(String communityID);
    public String updateCommunity(String communityName, String communityID);
    
    public String getObjectProperties(String objectName, String objectID);
    
    /* Collection */
    public String createCollection(String collectionName, String parentCommunityID);
    public String deleteCollection(String collectionID);
    public String updateCollection(String collectionName, String collectionID);
    
    /* Item */
    public String createItem(String itemName, String itemTitle, String collectionID);
    public String deleteItem(String itemID);
    public String itemAddMetadata(String itemID, String metadata);
    public String itemUpdateMetadata(String itemID, String metadata);
    public String itemClearMetadata(String itemID);
    public String itemGetAllBitstreams(String itemID);
    public String itemAddBitstream(String itemID, String bitstreamDescription);
    public String itemDeleteBitstream(String itemID, String bitstreamToDelete);
    
    
    /* Bitstreams */
    public String getAllBitstreams();
    public String downloadBitstream(String bitstreamID, String filenameToSave);
    
    // Not important methods
    public String getAllCommunities();
    public String getCommunityById(String id);
    public String getAllCollections();
    public String getCollectionById(String id);
    public String getAllItems();
    public String getItemById(String id);
    public String getItemMetadataById(String id);
    
    
    

}
