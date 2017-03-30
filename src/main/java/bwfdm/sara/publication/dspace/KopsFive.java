/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication.dspace;

/**
 *
 * @author vk
 */
public class KopsFive extends DSpaceVersionFive{

    // Constructor
    public KopsFive() {
        
        super(  "OPARU-5", 
                DSpaceConfig.URL_OPARU_FIVE, 
                DSpaceConfig.URL_OPARU_FIVE_REST, 
                DSpaceConfig.SSL_VERIFY_OPARU_FIVE,
                DSpaceConfig.RESPONSE_STATUS_OK_OPARU_FIVE);
        
        System.out.println("Constructor oparu-5.");      
    }
}
