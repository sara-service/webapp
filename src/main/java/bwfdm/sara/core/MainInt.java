/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.core;

import bwfdm.sara.repositories.DSpaceConfig;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vk
 */
public class MainInt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
             
        System.out.println(DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU));
    }
    
}
