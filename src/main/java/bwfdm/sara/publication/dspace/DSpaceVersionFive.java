/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication.dspace;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 *
 * @author vk
 */
abstract public class DSpaceVersionFive extends DSpaceGeneral {
    
    // Constructor
    public DSpaceVersionFive(String repoType, String urlServer, String urlRest, String verify, int responseStatusOK) {
        
        super(repoType, urlServer, urlRest, verify, responseStatusOK);
        System.out.println("Constructor DSpace-5.");      
    }

    
    @Override
    public boolean isRestEnable() {   
        Invocation.Builder invocationBuilder = testWebTarget.request(); 
        Response response = invocationBuilder.get();
        return response.readEntity(String.class).equals(DSpaceConfig.RESPONSE_REST_TEST_OPARU); //connection will be closed automatically after the readEntity
    }

    @Override
    public boolean login(String email, String password) {
        //Login command:
        //curl -H "Content-Type: application/json" --data '{"email":"admin@dspace.org", "password":"dspace"}' http://localhost:8080/rest/login
        
        System.out.println("--- " + repoType + ", login ---");
        
        boolean loginCorrect = false;
        Invocation.Builder invocationBuilder = loginWebTarget.request(); 
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
        String data = "{"
                + "\"email\":\"" + email + "\", "
                + "\"password\":\"" + password + "\""
                + "}";
        Response response = invocationBuilder.post(Entity.json(data));
        if (response.getStatus() == this.responseStatusOK){
            this.token = response.readEntity(String.class);
            loginCorrect = true;
        }
        System.out.println("response login: " + response.getStatus()); 
        response.close(); //not realy needed but better to close
        return loginCorrect;
    }

    @Override
    public boolean logout() {
        //Logout command:
        //curl -X POST -H "Content-Type: application/json" -H "rest-dspace-token: 1febef81-5eb6-4e76-a0ea-a5be245563a5" http://localhost:8080/rest/logout
        
        System.out.println("--- " + repoType + ", login ---");
        
        boolean logoutCorrect = false;
        Invocation.Builder invocationBuilder = logoutWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
        invocationBuilder.header("rest-dspace-token", this.token);
        Response response = invocationBuilder.post(Entity.json(""));
        if (response.getStatus() == this.responseStatusOK){
            this.token = response.readEntity(String.class);
            logoutCorrect = true;
        }
        System.out.println("response logout: " + response.getStatus());
        response.close(); //not realy needed but better to close
        return logoutCorrect;
    }
    
    @Override
    public boolean isAuthenticated(){    
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getConnectionStatus(){
        System.out.println("--- " + repoType + ", get connection status");
        
        Invocation.Builder invocationBuilder = statusWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_TYPE_OPARU);
        invocationBuilder.header("rest-dspace-token", this.token);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class);        
    }
    
    //@Override
    public String getToken() {      
        return this.token;
    }

  
    /* COMMUNITIES */
    
    
    @Override
    public String createCommunity(String communityName, String parentCommunityID) {
        System.out.println("--- " + repoType + ", create new community ---");
        
        WebTarget newCommunityWebTarget = communitiesWebTarget;
        if (!parentCommunityID.equals("")){
            newCommunityWebTarget = communitiesWebTarget.path(parentCommunityID).path("communities");
        }
        Invocation.Builder invocationBuilder = newCommunityWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
        invocationBuilder.header("rest-dspace-token", token);
        String data = "{"
                + "\"name\":" + "\"" + communityName + "\""
                + "}";
        System.out.println(data);
        System.out.println(newCommunityWebTarget.toString());
        //System.out.println(invocationBuilder.toString());
        Response response = invocationBuilder.post(Entity.json(data));
        System.out.println("response status - create community: " + response.getStatus());
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    @Override
    public String deleteCommunity(String communityID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String updateCommunity(String communityName, String communityID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getObjectProperties(String objectName, String objectID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    /* COLLECTIONS */

       
    @Override
    public String createCollection(String collectionName, String parentCommunityID) {
        System.out.println("--- " + repoType + ", create collection ---");
        
        WebTarget newCollectionWebTarget = communitiesWebTarget.path(parentCommunityID).path("collections");
        
        Invocation.Builder invocationBuilder = newCollectionWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
        invocationBuilder.header("rest-dspace-token", token);
        String data = "{"
                + "\"name\":" + "\"" + collectionName + "\""
                + "}";
        System.out.println(data);
        System.out.println(newCollectionWebTarget.toString());
        Response response = invocationBuilder.post(Entity.json(data));
        System.out.println("response status - create collection: " + response.getStatus());
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    @Override
    public String deleteCollection(String collectionID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String updateCollection(String collectionName, String collectionID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createItem(String itemName, String itemTitle, String collectionID) {
        System.out.println("--- " + repoType + ", create new item");
        
        WebTarget newItemWebTarget = collectionsWebTarget.path(collectionID).path("items");
        
        Invocation.Builder invocationBuilder = newItemWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_TYPE_OPARU);
        invocationBuilder.header("user", DSpaceConfig.EMAIL_OPARU);
        invocationBuilder.header("pass", DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU, this));
//        invocationBuilder.header("rest-dspace-token", this.getToken());
//        invocationBuilder.header("login", token);
        String data = "{"
                + "\"name\":" + "\"" + itemName + "\""
                + "}";
//        String data = "{"
//                + "\"metadata\":["
//                    + "{"
//                    + "\"key\":" + "\"dc.contributor.author\""
//                    + "\"value\":" + "\"" + itemName + "\""
//                    + "}"
//                + "]}";
        System.out.println(data);
        System.out.println(newItemWebTarget.toString());
        Response response = invocationBuilder.post(Entity.json(data));
        System.out.println("response status - create item: " + response.getStatus());
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    
    @Override
    public String deleteItem(String itemID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String addItemMetadata(String itemID, String metadata) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String updateItemMetadata(String itemID, String metadata) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String clearItemMetadata(String itemID){
        return "";
    }

    
    @Override
    public String addItemBitstream(String itemID, String bitstreamToAdd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String deleteItemBitstream(String itemID, String bitstreamToDelete) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String downloadBitstream(String bitstreamID, String filenameToSave) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
      
    
    /**
     * Methods from the "PublicationRepository" Interface
     */

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
