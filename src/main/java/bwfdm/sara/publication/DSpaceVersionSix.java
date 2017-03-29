/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication;

import bwfdm.sara.publication.dto.CookieStatusResponseDspace;
import bwfdm.sara.utils.JsonUtils;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author vk
 */
abstract public class DSpaceVersionSix extends DSpaceGeneral {
    
    public DSpaceVersionSix(String repoType, String urlServer, String urlRest, String verify, int responseStatusOK) {
        
        super(repoType, urlServer, urlRest, verify, responseStatusOK);
        System.out.println("Constructor DSpace-6.");      
    }
  
    
    /**
     * Login, email/password
     * 
     * @param email
     * @param password
     * @return 
     */
    @Override
    public boolean login(String email, String password) {
        // Login command:
        // curl -v -X POST --data "email=admin@dspace.org&password=mypass" https://dspace.myu.edu/rest/login
        System.out.println("--- " + repoType + ", login ---");
        
        // Check if login already done
        if (this.isAuthenticated()){
            return true;
        }
        System.out.println("cookie full string: " + this.getCookie());
        Invocation.Builder invocationBuilder = loginWebTarget.request(); 
        invocationBuilder.header("Content-Type", "application/x-www-form-urlencoded");
        Form form = new Form();
        form.param("email", email);
        form.param("password", password);
        Response response = invocationBuilder.post(Entity.form(form));
        if (response.getStatus() == this.responseStatusOK){
            this.setCookie(response.getCookies().get(DSpaceConfig.COOKIE_KEY_OPARU_SIX));
        }
        //this.cookie = response.getCookies().get(DSpaceConfig.COOKIE_KEY_OPARU_SIX);
        System.out.println("cookie full string: " + this.getCookie());
        System.out.println("response login: " + response.getStatus()); 
        System.out.println("response coockie: " + this.getCookie().getValue());
        System.out.println("cookie name: " + this.getCookie().getName());
        response.close(); //not realy needed but better to close
        return this.isAuthenticated();           
    }

    /**
     * Logout.
     * @return true or false 
     */
    @Override
    public boolean logout() {
        // Command:
        // curl -v -X POST --cookie "JSESSIONID=6B98CF8648BCE57DCD99689FE77CB1B8" https://dspace.myu.edu/rest/logout
        System.out.println("--- " + repoType + ", logout ---");
        
        Invocation.Builder invocationBuilder = logoutWebTarget.request(); 
        invocationBuilder.cookie(this.getCookie()); 
        //invocationBuilder.cookie(new Cookie("",""));
        Response response = invocationBuilder.post(Entity.json(null)); //ohne "--data"
        System.out.println("response logout: " + response.getStatus()); 
        System.out.println("response string: " + response.readEntity(String.class));
        System.out.println("cookie before = " + this.getCookie());
        if ((response.getStatus() == this.responseStatusOK) && !this.isAuthenticated()){ // even if cookie is wrong response Status is 200! 
            this.setCookie(new Cookie("", ""));
            System.out.println("cookie after = " + this.getCookie());
            return true;
        }        

        System.out.println("-------");
        return false;
    }
    
    /**
     * Check if authenticated
     * @return true/false
     */
    @Override
    public boolean isAuthenticated(){
        System.out.println("--- " + repoType + ", is authenticated ---");
        
        String status = this.getConnectionStatus();
        CookieStatusResponseDspace cookieStatus = JsonUtils.jsonStringToObject(status, CookieStatusResponseDspace.class);
        return cookieStatus.isAuthenticated();
    }
    
    /**
     * Check connection status.
     * @return response string, JSON
     */
    @Override
    public String getConnectionStatus(){
        System.out.println("--- " + repoType + ", get connection status");
       
        Invocation.Builder invocationBuilder = statusWebTarget.request();
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());     
        Response response = invocationBuilder.get();
        String status = response.readEntity(String.class); // Connection will be closed automatically       
        return status;
    }
   
    /**
     * Get cookie
     * @return Cookie
     */
    public Cookie getCookie(){      
        return this.cookie;
    }

    /**
     * Set cookie.
     * @param cookie 
     */
    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }
      
    
    /* COMMUNITIES */
    
  
    /**
     * Create new community
     * 
     * @param communityName
     * @param parentCommunityID
     * @return String, response 
     */
    @Override
    public String createCommunity(String communityName, String parentCommunityID) {
        System.out.println("--- " + repoType + ", create new community ---");
        
        WebTarget newCommunityWebTarget = communitiesWebTarget;
        if (!parentCommunityID.equals("")){
            newCommunityWebTarget = communitiesWebTarget.path(parentCommunityID).path("communities");
        }
        Invocation.Builder invocationBuilder = newCommunityWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        String data = "{"
                + "\"name\":" + "\"" + communityName + "\""
                + "}";
        Response response = invocationBuilder.post(Entity.json(data));
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    /**
     * Delete community
     * @param communityID
     * @return response string
     */
    @Override
    public String deleteCommunity(String communityID) {
        System.out.println("--- " + repoType + ", delete community ---");
        
        if (communityID.equals("")){
            return DSpaceConfig.RESPONSE_ERROR_JSON; //empty ID, error
        }
        
        WebTarget deleteCommunityWebTarget = communitiesWebTarget;
        deleteCommunityWebTarget = communitiesWebTarget.path(communityID);

        Invocation.Builder invocationBuilder = deleteCommunityWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        
        Response response = invocationBuilder.delete();
        
        System.out.println("delete, response: " + response.getStatus());
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class);
    }

    /**
     * Update community
     * 
     * @param newCommunityName
     * @param communityID
     * @return 
     */
    @Override
    public String updateCommunity(String newCommunityName, String communityID) {
        System.out.println("--- " + repoType + ", update community ---");
        
        if (communityID.equals("")){
            return DSpaceConfig.RESPONSE_ERROR_JSON; //empty ID, error
        }
        
        WebTarget newCommunityWebTarget = communitiesWebTarget;
        newCommunityWebTarget = communitiesWebTarget.path(communityID);
        
        Invocation.Builder invocationBuilder = newCommunityWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        String data = "{"
                + "\"name\":" + "\"" + newCommunityName + "\""
                + "}";
        Response response = invocationBuilder.put(Entity.json(data));
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity" 
    }

    @Override
    public String getObjectProperties(String objectName, String objectID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    /* COLLECTIONS */
    
     
    /**
     * Create collection
     * 
     * @param collectionName
     * @param parentCommunityID
     * @return response string
     */
    @Override
    public String createCollection(String collectionName, String parentCommunityID) {
        System.out.println("--- " + repoType + ", create collection ---");
        
        WebTarget newCollectionWebTarget = communitiesWebTarget.path(parentCommunityID).path("collections");
        
        Invocation.Builder invocationBuilder = newCollectionWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        String data = "{"
                + "\"name\":" + "\"" + collectionName + "\""
                + "}";
        Response response = invocationBuilder.post(Entity.json(data));
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    /**
     * Delete collection
     * 
     * @param collectionID
     * @return response string
     */
    @Override
    public String deleteCollection(String collectionID) {
        System.out.println("--- " + repoType + ", delete collection ---");
        
        WebTarget newCollectionWebTarget = collectionsWebTarget.path(collectionID);
        
        Invocation.Builder invocationBuilder = newCollectionWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        
        Response response = invocationBuilder.delete();
        
        System.out.println("delete, response: " + response.getStatus());
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    
    }

    /**
     * Update collection (name)
     * 
     * @param newCollectionName
     * @param collectionID
     * @return 
     */
    @Override
    public String updateCollection(String newCollectionName, String collectionID) {
        System.out.println("--- " + repoType + ", update collection --");
        
        if (collectionID.equals("")){
            return "Update collection, error: empty ID."; //empty ID, error
        }
        
        WebTarget collectionWebTarget = collectionsWebTarget;
        collectionWebTarget = collectionsWebTarget.path(collectionID);
        
        Invocation.Builder invocationBuilder = collectionWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        String data = "{"
                + "\"name\":" + "\"" + newCollectionName + "\""
                + "}";
        Response response = invocationBuilder.put(Entity.json(data));
        
        System.out.println("update, response: " + response.getStatus());
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity" 
    
    }

    
    /* ITEMS */
    
    
    /**
     * Create new item
     * 
     * @param itemName
     * @param itemTitle
     * @param collectionID
     * @return response string, JSON
     */
    @Override
    public String createItem(String itemName, String itemTitle, String collectionID) {
        System.out.println("--- " + repoType + ", create new item");
        
        if (collectionID.equals("")){
            return DSpaceConfig.RESPONSE_ERROR_JSON; //empty ID, error
        }
        
        WebTarget newItemWebTarget = collectionsWebTarget.path(collectionID).path("items");
        
        Invocation.Builder invocationBuilder = newItemWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        String data = "{"
                + "\"name\":" + "\"" + itemName + "\""
                + "}";
        Response response = invocationBuilder.post(Entity.json(data));
        
        System.out.println("create item, response: " + response.getStatus());
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    
    }

    /**
     * Delete item by ID
     * @param itemID
     * @return 
     */
    @Override
    public String deleteItem(String itemID) {
        System.out.println("--- " + repoType + ", delete item by ID ---");
        
        WebTarget itemWebTarget = itemsWebTarget;
        itemWebTarget = itemsWebTarget.path(itemID);
        
        Invocation.Builder invocationBuilder = itemWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        
        Response response = invocationBuilder.delete();
        
        System.out.println("delete item, response: " + response.getStatus());
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        return response.readEntity(String.class);
    }

    
    //TODO:
    // 1. Escape characters (\n.....)
    // 2. Special metadata field for the GitLab link.
    //
    /**
     * Add metadata to the item
     * 
     * @param itemID
     * @param metadata
     * @return 
     * 
     * TODO:
     * 1. Escape characters (\n.....)
     * 2. Special metadata field for the GitLab link.
     */
    @Override
    public String addItemMetadata(String itemID, String metadata) {
        System.out.println("--- " + repoType + ", add metadata to the item ---");
        
        WebTarget itemMetadataWebTarget = itemsWebTarget;
        itemMetadataWebTarget = itemsWebTarget.path(itemID).path("metadata");
        
        Invocation.Builder invocationBuilder = itemMetadataWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        String data = metadata;
        
        Response response = invocationBuilder.post(Entity.json(data));
        
        System.out.println("add item metadata, response: " + response.getStatus());
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class);
    }

    /**
     * Update metadata of the item
     * 
     * @param itemID
     * @param metadata
     * @return 
     */
    @Override
    public String updateItemMetadata(String itemID, String metadata) {
        System.out.println("--- " + repoType + ", update metadata of the item ---");
          
        WebTarget itemMetadataWebTarget = itemsWebTarget;
        itemMetadataWebTarget = itemsWebTarget.path(itemID).path("metadata");
        
        Invocation.Builder invocationBuilder = itemMetadataWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        String data = metadata;
        
        Response response = invocationBuilder.put(Entity.json(data));
        
        System.out.println("add item metadata, response: " + response.getStatus());
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        return response.readEntity(String.class);
    }

    /**
     * Clear metadata of the item. 
     * Remain only "dc.date.accessioned" and "dc.date.available"
     * 
     * @param itemID
     * @return response string
     */
    @Override
    public String clearItemMetadata(String itemID){
        
        System.out.println("--- " + repoType + ", clear metadata of the item ---");
          
        WebTarget itemMetadataWebTarget = itemsWebTarget;
        itemMetadataWebTarget = itemsWebTarget.path(itemID).path("metadata");
        
        Invocation.Builder invocationBuilder = itemMetadataWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        
        Response response = invocationBuilder.delete();
        
        System.out.println("clear item metadata, response: " + response.getStatus());
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        return response.readEntity(String.class);
    }
    
    
        
    @Override
    public String addItemBitstream(String itemID, String bitstreamDescription) {
        System.out.println("--- " + repoType + ", add bitstream to the item ---");
        
        WebTarget itemBitstreamWebTarget = itemsWebTarget;
        itemBitstreamWebTarget = itemsWebTarget.path(itemID).path("bitstreams");
        
        Invocation.Builder invocationBuilder = itemBitstreamWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        invocationBuilder.cookie(this.getCookie());
        
        String data = bitstreamDescription;
        
//        String data = "{"
//                + "\"name\":" + "\"" + "test-bitstream" + "\""
//                + "}";
        
        Response response = invocationBuilder.post(Entity.json(data));
        
        System.out.println("add item bitstream, response: " + response.getStatus());
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class);
    }

    @Override
    public String deleteItemBitstream(String itemID, String bitstreamToDelete) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String downloadBitstream(String bitstreamID, String filenameToSave) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     

    /* PUBLICATION REPOSITORY methods */
    

    @Override
    public boolean loginPublicationRepository() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean logoutPublicationRepository() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void publishElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeElementMetadata() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getRepositoryUrl(){
        return this.urlServer;
    }
    
}
