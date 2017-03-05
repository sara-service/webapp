/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.repositories;

import bwfdm.sara.core.MainInt;
import bwfdm.sara.metadata.MetadataCollection;
import bwfdm.sara.utils.WebUtils;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;


/**
 *
 * @author vk
 */
public class Oparu implements DSpaceREST,PublicationRepository{

    private String urlServer = "";
    private String urlRest = "";
    private String verify = "";
    private String token = "";
    
    private Client client;
    private final WebTarget restWebTarget;
    private final WebTarget loginWebTarget;
    private final WebTarget logoutWebTarget;
    private final WebTarget testWebTarget;
    private final WebTarget statusWebTarget;
    
    private final int responseStatusOK = 200;
        
    // Constructor
    public Oparu() {
        this.urlServer = DSpaceConfig.URL_OPARU;
        this.urlRest = DSpaceConfig.URL_OPARU_REST;
        this.verify = DSpaceConfig.SSL_VERIFY_OPARU;
        
        //REST-client
        this.client = ClientBuilder.newClient();
        if (DSpaceConfig.SSL_VERIFY_OPARU.equals("false")){
            try { 
                this.client = WebUtils.IgnoreSSLClient(); //Ignore SSL-Verification
            }
            catch (Exception ex) { 
                Logger.getLogger(MainInt.class.getName()).log(Level.SEVERE, null, ex); 
            }
        }
        
        //WebTargets
        this.restWebTarget = this.client.target(DSpaceConfig.URL_OPARU_REST);
        this.loginWebTarget = this.restWebTarget.path("login");
        this.logoutWebTarget = this.restWebTarget.path("logout");
        this.testWebTarget = this.restWebTarget.path("test");
        this.statusWebTarget = this.restWebTarget.path("status");
    }


       
    @Override
    public boolean isRestEnable() {
        
        Invocation.Builder invocationBuilder = testWebTarget.request(); 
        Response response = invocationBuilder.get();
        return response.readEntity(String.class).equals(DSpaceConfig.RESPONSE_TEST_OPARU); //connection will be closed automatically after the readEntity
    }

    @Override
    public boolean login(String email, String password) {
        //Login command:
        //curl -H "Content-Type: application/json" --data '{"email":"admin@dspace.org", "password":"dspace"}' http://localhost:8080/rest/login
        
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
    public String getTokenStatus(String token){
        
        Invocation.Builder invocationBuilder = statusWebTarget.request();
        invocationBuilder.header("Content-Type", DSpaceConfig.HEADER_CONTENT_TYPE_OPARU);
        invocationBuilder.header("Accept", DSpaceConfig.HEADER_ACCEPT_OPARU);
        invocationBuilder.header("rest-dspace-token", token);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class);        
    }
    
    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public String createCommunity(String communityName, String parentCommunityID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String deleteCommunity(String communityID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean updateCommunity(String communityName, String communityID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getObjectProperties(String objectName, String objectID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createCollection(String collectionName, String parentCommunityID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String deleteCollection(String collectionID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean updateCollection(String collectionName, String collectionID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createItem(String itemName, String itemTitle, String collectionID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deleteItem(String itemID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delteItemInCollection(String collectionID, String itemID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean itemAddMetadata(String itemID, MetadataCollection collectionWithMetadata) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    
   
}
