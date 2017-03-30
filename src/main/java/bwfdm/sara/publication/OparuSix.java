/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionFive;
import bwfdm.sara.publication.dspace.DSpaceVersionSix;

/**
 *
 * @author vk
 */
public class OparuSix implements PublicationRepository{

    private DSpaceVersionSix dspaceSix;
    private final String urlServer;
            
    // Constructor
    public OparuSix() {
        
        urlServer = DSpaceConfig.URL_OPARU_SIX;
                
        dspaceSix = new DSpaceVersionSix(
                "OPARU-6", 
                DSpaceConfig.URL_OPARU_SIX, 
                DSpaceConfig.URL_OPARU_SIX_REST, 
                DSpaceConfig.SSL_VERIFY_OPARU_SIX,
                DSpaceConfig.RESPONSE_STATUS_OK_OPARU_SIX);
               
        System.out.println("--- OPARU-6, constructor ---");
    }
    
    
    /* PUBLICATION REPOSITORY methods */
    

    @Override
    public boolean loginPublicationRepository() { 
        if(!dspaceSix.isRestEnable()){
            return false;
        }
        return dspaceSix.login(DSpaceConfig.PASSWORD_OPARU_SIX, DSpaceConfig.getPassword(DSpaceConfig.PASSWORD_OPARU_SIX, this));    
    }

    @Override
    public boolean logoutPublicationRepository() {
        return dspaceSix.logout();
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
