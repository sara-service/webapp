/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionFive;
import bwfdm.sara.publication.dspace.DSpaceVersionSix;
import bwfdm.sara.publication.dspace.dto.CommunityObjectDSpaceSix;
import bwfdm.sara.utils.JsonUtils;

/**
 *
 * @author vk
 */
public class OparuSix implements PublicationRepository{

    private DSpaceVersionSix dspaceRepo;
    private final String urlServer;
     
            
    // Constructor
    public OparuSix() {
        
        urlServer = DSpaceConfig.URL_OPARU_SIX;
                
        dspaceRepo = new DSpaceVersionSix(
                "OPARU-6", 
                DSpaceConfig.URL_OPARU_SIX, 
                DSpaceConfig.URL_OPARU_SIX_REST, 
                DSpaceConfig.SSL_VERIFY_OPARU_SIX,
                DSpaceConfig.RESPONSE_STATUS_OK_OPARU_SIX);
               
        System.out.println("--- OPARU-6, constructor ---");
    }
    

    @Override
    public boolean loginPublicationRepository() { 
        if(!dspaceRepo.isRestEnable()){
            return false;
        }
        return dspaceRepo.login(DSpaceConfig.PASSWORD_OPARU_SIX, DSpaceConfig.getPassword(DSpaceConfig.PASSWORD_OPARU_SIX, this));    
    }

    
    @Override
    public boolean logoutPublicationRepository() {
        return dspaceRepo.logout();
    }
    
    
    
    @Override
    public boolean publishElement(String publicationLink, String metadata) {
        
        boolean isPublished = true;
        String communityID = "";
        String collectionID = "";
        String itemID = "";
        
        if(!dspaceRepo.isAuthenticated()){
            return false;
        }
                
        // Get community ID / create community
        boolean communityExists = false;
        CommunityObjectDSpaceSix[] communities = JsonUtils.jsonStringToObject(dspaceRepo.getAllCommunities(), CommunityObjectDSpaceSix[].class);
        for(CommunityObjectDSpaceSix comm  : communities){
            if(comm.name.equals(PublicationConfig.SARA_COMMUNITY_NAME)){
                communityExists = true;
                communityID = comm.uuid;
                break;
            }
        }
        if(!communityExists){
            String response = dspaceRepo.createCommunity(PublicationConfig.SARA_COMMUNITY_NAME, "");
            communityID = JsonUtils.jsonStringToObject(response, CommunityObjectDSpaceSix.class).uuid;
        }
        
        // Get collection ID / create collection
        
        
        
        
        // Create item
        
        // Update item metadata 
        
        return true;
    }

    @Override
    public String changeElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String deleteElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String changeElementMetadata() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getRepositoryUrl(){
        return this.urlServer;
    }
    
}
