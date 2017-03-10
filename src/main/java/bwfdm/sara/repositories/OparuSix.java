/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.repositories;

import bwfdm.sara.core.MainInt;
import bwfdm.sara.metadata.MetadataCollection;
import bwfdm.sara.utils.WebUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
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
public class OparuSix implements DSpaceREST,PublicationRepository {

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

    private String coockie = "";
    private Client client;

    // Constructor
    public OparuSix() {
        
        System.out.println("Constructor Oparu-6.");
        
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
        
        Logger logger = Logger.getLogger(getClass().getName());
        Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        client.register(feature);
        
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
        
        System.out.println("login oparu-6");
        
        //TODO: encode correct!
        
//        try {
//            email = URLEncoder.encode(email, "ISO-8859-1");
//            password = URLEncoder.encode(password, "ISO-8859-1");
//            System.out.println("email + password encoded: " + email + " " + password);
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(OparuSix.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        boolean loginCorrect = false;
        Invocation.Builder invocationBuilder = loginWebTarget.request(); 
        invocationBuilder.header("Content-Type", "application/x-www-form-urlencoded");
        //invocationBuilder.header("Content-Type", "application/json");
        //String data = "email=" + email + "&password=" + password;
        //String data = "email=project-sara@uni-konstanz.de&password=SaraTest";
        //System.out.println(data);
        //System.out.println(loginWebTarget.toString());
        Form form = new Form();
        form.param("email", email);
        form.param("password", password);
        Response response = invocationBuilder.post(Entity.form(form));
        if (response.getStatus() == this.responseStatusOK){
            this.coockie = response.readEntity(String.class);
            loginCorrect = true;
        }
        System.out.println("response login: " + response.getStatus()); 
        System.out.println("response string: " + this.coockie);
        response.close(); //not realy needed but better to close
        return loginCorrect;        
    }

    @Override
    public boolean logout() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return this.coockie;
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
    public boolean deleteItemInCollection(String collectionID, String itemID) {
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

    @Override
    public String getAllCommunities() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCommunityById(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAllCollections() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCollectionById(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAllItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getItemById(String id) {
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

    
}
