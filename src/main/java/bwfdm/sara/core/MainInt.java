/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.core;

import bwfdm.sara.repositories.DSpaceConfig;
import bwfdm.sara.repositories.Oparu;
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
        String tokenOparu = "";
        Oparu oparu = new Oparu();
        boolean isOK;
        
        System.out.println("Is REST enable: " + oparu.isRestEnable());
        
        for (int i = 1; i <= 2; i++) {
            System.out.println("---" + i + "---");
            isOK = oparu.login(DSpaceConfig.EMAIL_OPARU, DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU));
            System.out.println("login OK: " + isOK);
            System.out.println("token login: " + oparu.getToken());
            isOK = oparu.logout();
            System.out.println("logout OK: " + isOK);
            System.out.println("token logout: " + oparu.getToken());
        }
        
    }
    
}
