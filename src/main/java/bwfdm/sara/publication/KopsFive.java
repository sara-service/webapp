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
public class KopsFive extends OparuFive{

    private DSpaceVersionFive dspaceFive;
    private final String urlServer;
    
    // Constructor
    public KopsFive() {
        
        urlServer = DSpaceConfig.URL_OPARU_FIVE;
                
        dspaceFive = new DSpaceVersionFive(
                "KOPS-5", 
                DSpaceConfig.URL_KOPS_FIVE, 
                DSpaceConfig.URL_KOPS_FIVE_REST, 
                DSpaceConfig.SSL_VERIFY_KOPS_FIVE,
                DSpaceConfig.RESPONSE_STATUS_OK_KOPS_FIVE);
        
        System.out.println("Constructor oparu-5.");      
    }
}
