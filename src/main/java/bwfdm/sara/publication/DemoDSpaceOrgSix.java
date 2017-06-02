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
public class DemoDSpaceOrgSix extends OparuSix{

    private DSpaceVersionSix dspaceRepo;
    private final String urlServer;
    
    public DemoDSpaceOrgSix(){
        urlServer = DSpaceConfig.URL_DemoDSpaceOrg_SIX;
                
        dspaceRepo = new DSpaceVersionSix(
                "DemoDSpaceOrg-6", 
                DSpaceConfig.URL_DemoDSpaceOrg_SIX, 
                DSpaceConfig.URL_DemoDSpaceOrg_SIX_REST, 
                DSpaceConfig.SSL_VERIFY_DemoDSpaceOrg_SIX,
                DSpaceConfig.RESPONSE_STATUS_OK_DemoDSpaceOrg_SIX);
               
        System.out.println("--- DemoDSpaceOrg-6, constructor ---");
    }
    
    @Override
    public boolean loginPublicationRepository() { 
        if(!dspaceRepo.isRestEnable()){
            return false;
        }
        return dspaceRepo.login(DSpaceConfig.PASSWORD_DemoDSpaceOrg_SIX, DSpaceConfig.getPassword(DSpaceConfig.PASSWORD_DemoDSpaceOrg_SIX, this));    
    }
    
    @Override
    public String getRepositoryUrl(){
        return this.urlServer;
    }
}
