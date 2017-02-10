/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.repositories;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Configuration of DSpace: login, password, types...
 * 
 * TODO: create secure(!) authentification!!! Remove login/password!
 * 
 * @author vk
 */
public class DSpaceConfig {
    
    /** 
     * OPARU config
     */
    public static final String URL_OPARU = "https://vtstest.rz.uni-ulm.de";
    public static final String EMAIL_OPARU = "project-sara@uni-konstanz.de";
    public static final String PASSWORD_OPARU = "SaraTest";
    public static final String REQUEST_TYPE_OPARU = "json";
    public static final String VERIFY_OPARU = "false";
    
    public static final String HEADER_CONTENT_TYPE_OPARU = 
            " {" + 
            " \"Content-Type\": \"application/" + REQUEST_TYPE_OPARU + "\"" + 
            " }";
    public static final String HEADER_ACCEPT_OPARU = 
            " {" + 
            " \"Accept\": \"application/" + REQUEST_TYPE_OPARU + "\"" + 
            " }";
    public static final String HEADER_TOKEN_OPARU = 
            " {" + 
            " \"rest-dspace-token\": \"\"" + 
            " }";


    /** 
     * KOPS config
     */
    
    
    
//    /**
//     * Get password 
//     * Read file "login.txt"
//     * Format:
//     * email:password
//     * @param email
//     * @return password string
//     */
//    public static String getPasswordFromFile(String email){
//        
//        //String filename = System.getProperty("java.class.path") + "/login.txt";
//        //String filename = DSpaceConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "\\login.txt" ;
//                
//        // TODO: Not clear, what is the correct path to the file
//        String filename = "login.txt";
//        String password = "";
//        BufferedReader br;
//               
//        try {
//            //br = new BufferedReader(new FileReader(DSpaceConfig.class.getResource(filename).toString()));
//            br = new BufferedReader(new FileReader(filename));
//            String line = br.readLine();
//
//            while (line != null) {
//                if(line.startsWith(email)){
//                    password = line.split(":")[1];
//                    break;
//                }
//                line = br.readLine();
//            }
//            br.close();
//        } catch(IOException ex) {
//            Logger.getLogger(DSpaceConfig.class.getName()).log(Level.SEVERE, null, ex); 
//        }
//        return password;
//    }
    
    
    
    
}
