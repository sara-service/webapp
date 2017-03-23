/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.repositories;

import bwfdm.sara.core.MainInt;
import bwfdm.sara.metadata.MetadataCollection;
import bwfdm.sara.rest.CookieStatusResponseDto;
import bwfdm.sara.utils.JsonUtils;
import bwfdm.sara.utils.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.logging.LoggingFeature;
import sun.nio.cs.ISO_8859_2;

/**
 *
 * @author vk
 */
public class OparuSix implements DSpace {

    private final String urlServer;
    private final String urlRest;
    private final String verify;
    private final int responseStatusOK;
    
    private final WebTarget restWebTarget;
    private final WebTarget loginWebTarget;
    private final WebTarget logoutWebTarget;
    private final WebTarget testWebTarget;
    private final WebTarget statusWebTarget;
    private final WebTarget communitiesWebTarget;
    private final WebTarget collectionsWebTarget;
    private final WebTarget itemsWebTarget;
    private final WebTarget bitstreamsWebTarget;
    private final WebTarget handleWebTarget;    

    private Cookie cookie;
    private Client client;

    // Constructor
    public OparuSix() {
        
        System.out.println("--- Constructor Oparu-6 ---");
        
        this.urlServer = DSpaceConfig.URL_OPARU_SIX;
        this.urlRest = DSpaceConfig.URL_OPARU_SIX_REST;
        this.verify = DSpaceConfig.SSL_VERIFY_OPARU_SIX;
        this.responseStatusOK = DSpaceConfig.RESPONSE_STATUS_OK_OPARU_SIX;
        
        // REST-client
        this.client = ClientBuilder.newClient();
        if (this.verify.equals("false")){
            try { 
                this.client = WebUtils.IgnoreSSLClient(); //Ignore SSL-Verification
            }
            catch (Exception ex) { 
                Logger.getLogger(MainInt.class.getName()).log(Level.SEVERE, null, ex); 
            }
        }
        
        // WebTargets
        this.restWebTarget = this.client.target(DSpaceConfig.URL_OPARU_SIX_REST);
        this.loginWebTarget = this.restWebTarget.path("login");
        this.logoutWebTarget = this.restWebTarget.path("logout");
        this.testWebTarget = this.restWebTarget.path("test");
        this.statusWebTarget = this.restWebTarget.path("status");
        this.communitiesWebTarget = this.restWebTarget.path("communities");
        this.collectionsWebTarget = this.restWebTarget.path("collections");
        this.itemsWebTarget = this.restWebTarget.path("items");
        this.bitstreamsWebTarget = this.restWebTarget.path("bitstreams");
        this.handleWebTarget = this.restWebTarget.path("handle");
        
        // Cookies
        this.cookie = new Cookie("", "");
        
        //Logging of all requests/responses
        Logger logger = Logger.getLogger(getClass().getName());
        Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        client.register(feature);
    }
    
    @Override
    public boolean isRestEnable() {
        Invocation.Builder invocationBuilder = testWebTarget.request(); 
        Response response = invocationBuilder.get();
        return response.readEntity(String.class).equals(DSpaceConfig.RESPONSE_TEST_OPARU); //connection will be closed automatically after the readEntity
    }
    
    @Override
    public boolean login(String email, String password) {
        // Login command:
        // curl -v -X POST --data "email=admin@dspace.org&password=mypass" https://dspace.myu.edu/rest/login
        
        System.out.println("--- login ---");
        
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

    @Override
    public boolean logout() {
        // Command:
        // curl -v -X POST --cookie "JSESSIONID=6B98CF8648BCE57DCD99689FE77CB1B8" https://dspace.myu.edu/rest/logout
        System.out.println("--- logout ---");
        
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
    
    @Override
    public boolean isAuthenticated(){
        
        String status = this.getConnectionStatus();
        CookieStatusResponseDto cookieStatus = JsonUtils.jsonStringToObject(status, CookieStatusResponseDto.class);
        return cookieStatus.isAuthenticated();
    }
    
    @Override
    public String getConnectionStatus(){
        
        Invocation.Builder invocationBuilder = statusWebTarget.request();
        invocationBuilder.header("Accept", "application/json");
        invocationBuilder.cookie(this.getCookie());     
        Response response = invocationBuilder.get();
        String status = response.readEntity(String.class); // Connection will be closed automatically       
        return status;
    }
   
    public Cookie getCookie(){      
        return this.cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }
      
    
    /* COMMUNITIES */
    
    
    @Override
    public String getAllCommunities(){
        
        Invocation.Builder invocationBuilder = communitiesWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    @Override
    public String getCommunityById(String id){
        
        WebTarget communityIdWebTarget = communitiesWebTarget.path(id);
        Invocation.Builder invocationBuilder = communityIdWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }
    
    
    @Override
    public String createCommunity(String communityName, String parentCommunityID) {
        
        WebTarget newCommunityWebTarget = communitiesWebTarget;
        if (!parentCommunityID.equals("")){
            newCommunityWebTarget = communitiesWebTarget.path(parentCommunityID).path("communities");
        }
        Invocation.Builder invocationBuilder = newCommunityWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
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

    @Override
    public String deleteCommunity(String communityID) {
        
        if (communityID.equals("")){
            return DSpaceConfig.RESPONSE_ERROR_JSON; //empty ID, error
        }
        
        WebTarget deleteCommunityWebTarget = communitiesWebTarget;
        deleteCommunityWebTarget = communitiesWebTarget.path(communityID);

        Invocation.Builder invocationBuilder = deleteCommunityWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        invocationBuilder.cookie(this.getCookie());
        
        Response response = invocationBuilder.delete();
        
        System.out.println("delete, response: " + response.getStatus());
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class);
    }

    @Override
    public String updateCommunity(String newCommunityName, String communityID) {
        
        if (communityID.equals("")){
            return DSpaceConfig.RESPONSE_ERROR_JSON; //empty ID, error
        }
        
        WebTarget newCommunityWebTarget = communitiesWebTarget;
        newCommunityWebTarget = communitiesWebTarget.path(communityID);
        
        Invocation.Builder invocationBuilder = newCommunityWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
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
    
    
    @Override
    public String getAllCollections(){
        
        Invocation.Builder invocationBuilder = collectionsWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    @Override
    public String getCollectionById(String id){
        
        WebTarget communityIdWebTarget = collectionsWebTarget.path(id);
        Invocation.Builder invocationBuilder = communityIdWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }
       
    @Override
    public String createCollection(String collectionName, String parentCommunityID) {
        
        WebTarget newCollectionWebTarget = communitiesWebTarget.path(parentCommunityID).path("collections");
        
        Invocation.Builder invocationBuilder = newCollectionWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
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

    @Override
    public String deleteCollection(String collectionID) {
        
        WebTarget newCollectionWebTarget = collectionsWebTarget.path(collectionID);
        
        Invocation.Builder invocationBuilder = newCollectionWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        invocationBuilder.cookie(this.getCookie());
        
        Response response = invocationBuilder.delete();
        
        System.out.println("delete, response: " + response.getStatus());
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    
    }

    @Override
    public String updateCollection(String newCollectionName, String collectionID) {
        
        if (collectionID.equals("")){
            return "Update collection, error: empty ID."; //empty ID, error
        }
        
        WebTarget collectionWebTarget = collectionsWebTarget;
        collectionWebTarget = collectionsWebTarget.path(collectionID);
        
        Invocation.Builder invocationBuilder = collectionWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
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
    
    
    @Override
    public String getAllItems() {
        
        Invocation.Builder invocationBuilder = itemsWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        return response.readEntity(String.class);
    }

    @Override
    public String getItemById(String id) {
        
        WebTarget itemIdWebTarget = itemsWebTarget.path(id);
        Invocation.Builder invocationBuilder = itemIdWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }        
        return response.readEntity(String.class);
    }
    
    @Override
    public String getItemMetadataById(String id) {
        
        WebTarget itemIdWebTarget = itemsWebTarget.path(id).path("metadata");
        Invocation.Builder invocationBuilder = itemIdWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        } 
        return response.readEntity(String.class);
    }
    
    @Override
    public String createItem(String itemName, String itemTitle, String collectionID) {
    
        if (collectionID.equals("")){
            return DSpaceConfig.RESPONSE_ERROR_JSON; //empty ID, error
        }
        
        WebTarget newItemWebTarget = collectionsWebTarget.path(collectionID).path("items");
        
        Invocation.Builder invocationBuilder = newItemWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
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

    @Override
    public boolean deleteItem(String itemID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deleteItemInCollection(String collectionID, String itemID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    //TODO:
    // 1. Escape characters (\n.....)
    // 2. Special metadata field for the GitLab link.
    //
    @Override
    public String itemAddMetadata(String itemID, String metadata) {
        
        System.out.println("--- add item metadata ---");
        
        WebTarget itemMetadataWebTarget = itemsWebTarget;
        itemMetadataWebTarget = itemsWebTarget.path(itemID).path("metadata");
        
        Invocation.Builder invocationBuilder = itemMetadataWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_APPLICATION_JSON);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        invocationBuilder.cookie(this.getCookie());
        String data = metadata;
        
        Response response = invocationBuilder.put(Entity.json(data));
        
        System.out.println("add item metadata, response: " + response.getStatus());
        
        if (response.getStatus() != this.responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
 
        return response.readEntity(String.class);
    }

    @Override
    public boolean itemUpdateMetadata(String itemID, String metadataEntry) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String itemAddBitstream(String itemID, String bitstreamToAdd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean itemDeleteBitstream(String itemID, String bitstreamToDelete) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean downloadBitstream(String bitstreamID, String filenameToSave) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   


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
