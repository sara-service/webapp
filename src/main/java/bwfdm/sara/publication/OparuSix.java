/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication;

/**
 *
 * @author vk
 */
public class OparuSix extends DSpaceVersionSix{

    // Constructor
    public OparuSix() {
        
        super(  "OPARU-6", 
                DSpaceConfig.URL_OPARU_SIX, 
                DSpaceConfig.URL_OPARU_SIX_REST, 
                DSpaceConfig.SSL_VERIFY_OPARU_SIX,
                DSpaceConfig.RESPONSE_STATUS_OK_OPARU_SIX);
        
        System.out.println("--- OPARU-6, constructor ---");
    }  
}
