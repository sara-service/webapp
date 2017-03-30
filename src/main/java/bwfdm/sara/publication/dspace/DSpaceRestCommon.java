/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication.dspace;

import bwfdm.sara.gui.MainInt;
import bwfdm.sara.publication.dspace.dto.StatusObjectDSpaceSix;
import bwfdm.sara.utils.JsonUtils;
import bwfdm.sara.utils.WebUtils;
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

/**
 *
 * @author vk
 */
abstract public class DSpaceRestCommon implements DSpaceRest{

    protected final String urlServer;
    protected final String urlRest;
    protected final String verify;
    protected final int responseStatusOK;
    
    protected final String repoType;
    
    protected final WebTarget restWebTarget;
    protected final WebTarget loginWebTarget;
    protected final WebTarget logoutWebTarget;
    protected final WebTarget testWebTarget;
    protected final WebTarget statusWebTarget;
    protected final WebTarget communitiesWebTarget;
    protected final WebTarget collectionsWebTarget;
    protected final WebTarget itemsWebTarget;
    protected final WebTarget bitstreamsWebTarget;
    protected final WebTarget handleWebTarget;    

    protected String token;
    protected Cookie cookie;
    protected Client client;

    // Constructor
    public DSpaceRestCommon(String repoType, String urlServer, String urlRest, String verify, int responseStatusOK) {
        
        System.out.println("--- OPARU general implementation, constructor. Configuration for: " + repoType + " ---");
        
        this.repoType = repoType;
        
        this.urlServer = urlServer;
        this.urlRest = urlRest;
        this.verify = verify;
        this.responseStatusOK = responseStatusOK;
        
        // REST-client
        client = ClientBuilder.newClient();
        if (this.verify.equals("false")){
            try { 
                client = WebUtils.IgnoreSSLClient(); //Ignore SSL-Verification
            }
            catch (Exception ex) { 
                Logger.getLogger(MainInt.class.getName()).log(Level.SEVERE, null, ex); 
            }
        }
        
        // WebTargets
        restWebTarget = client.target(this.urlRest);
        loginWebTarget = restWebTarget.path("login");
        logoutWebTarget = restWebTarget.path("logout");
        testWebTarget = restWebTarget.path("test");
        statusWebTarget = restWebTarget.path("status");
        communitiesWebTarget = restWebTarget.path("communities");
        collectionsWebTarget = restWebTarget.path("collections");
        itemsWebTarget = restWebTarget.path("items");
        bitstreamsWebTarget = restWebTarget.path("bitstreams");
        handleWebTarget = restWebTarget.path("handle");
        
        // Cookie + token
        cookie = new Cookie("", "");
        token = "";
                
        
        //Logging of all requests/responses
        Logger logger = Logger.getLogger(getClass().getName());
        Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
        client.register(feature);
    }
    
    
    /**
     * Check if REST API is enable
     * @return 
     */
    @Override
    public boolean isRestEnable() {
        System.out.println("--- " + repoType + ", is REST enable ---");
        
        Invocation.Builder invocationBuilder = testWebTarget.request(); 
        Response response = invocationBuilder.get();
        return response.readEntity(String.class).equals(DSpaceConfig.RESPONSE_REST_TEST); //connection will be closed automatically after the readEntity
    }
       
    
    /* COMMUNITIES */
    
    
    /**
     * Get all communities
     * @return String, JSON
     */
    @Override
    public String getAllCommunities(){
        System.out.println("--- " + repoType + ", get all communities ---");
        
        Invocation.Builder invocationBuilder = communitiesWebTarget.path("top-communities").request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    
    /**
     * Get community by ID
     * @param id
     * @return String, JSON
     */
    @Override
    public String getCommunityById(String id){
        System.out.println("--- " + repoType + ", get community by ID ---");
        
        WebTarget communityIdWebTarget = communitiesWebTarget.path(id);
        Invocation.Builder invocationBuilder = communityIdWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }
    
   
    /* COLLECTIONS */
    
    
    /**
     * Get all collections
     * 
     * @return JSON string 
     */
    @Override
    public String getAllCollections(){
        System.out.println("--- " + repoType + ", get all collections ---");
        
        Invocation.Builder invocationBuilder = collectionsWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }

    
    /**
     * Get collection by ID
     * 
     * @param id
     * @return JSON string
     */
    @Override
    public String getCollectionById(String id){
        System.out.println("--- " + repoType + ", get collection by ID ---");
        
        WebTarget communityIdWebTarget = collectionsWebTarget.path(id);
        Invocation.Builder invocationBuilder = communityIdWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class); //Connection will be closed automatically after the "readEntity"
    }
       
    
    /* ITEMS */
    
    
    /**
     * Get all items
     * @return 
     */
    @Override
    public String getAllItems() {
        System.out.println("--- " + repoType + ", get all items ---");
        
        Invocation.Builder invocationBuilder = itemsWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        return response.readEntity(String.class);
    }

    /**
     * Get item by the ID
     * 
     * @param id
     * @return JSON string
     */
    @Override
    public String getItemById(String id) {
        System.out.println("--- " + repoType + ", get item by ID ---");
        
        WebTarget itemIdWebTarget = itemsWebTarget.path(id);
        Invocation.Builder invocationBuilder = itemIdWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }        
        return response.readEntity(String.class);
    }
    
    @Override
    public String getItemMetadataById(String id) {
        System.out.println("--- " + repoType + ", get item metadta by ID ---");
        
        WebTarget itemIdWebTarget = itemsWebTarget.path(id).path("metadata");
        Invocation.Builder invocationBuilder = itemIdWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        } 
        return response.readEntity(String.class);
    }
    
   
    @Override
    public String getItemBitstreams(String itemID){
        System.out.println("--- " + repoType + ", get all bitstreams of the item ---");
        
        WebTarget itemBitstreamsWebTarget = itemsWebTarget.path(itemID).path("bitstreams");
        Invocation.Builder invocationBuilder = itemBitstreamsWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        } 
        return response.readEntity(String.class);
    }

    
    @Override
    public String getAllBitstreams(){
        System.out.println("--- " + repoType + ", get all bitstreams ---");
        
        Invocation.Builder invocationBuilder = bitstreamsWebTarget.request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);
        invocationBuilder.header("Accept", MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        
        if (response.getStatus() != responseStatusOK){
            return DSpaceConfig.RESPONSE_ERROR_JSON;
        }
        
        return response.readEntity(String.class);
    }
    
}
