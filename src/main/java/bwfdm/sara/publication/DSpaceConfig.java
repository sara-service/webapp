/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication;

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
     * OPARU-5 config
     */
    public static final String URL_OPARU_FIVE = "https://vtstest.rz.uni-ulm.de";
    public static final String URL_OPARU_FIVE_REST = "https://vtstest.rz.uni-ulm.de/rest";   
    public static final String SSL_VERIFY_OPARU_FIVE = "false";
    public static final int RESPONSE_STATUS_OK_OPARU_FIVE = 200;
    public static final String PASSWORD_OPARU_FIVE = "SaraTest"; 
        
    /** 
     * OPARU-6 config
     */
    public static final String URL_OPARU_SIX = "https://bib-test.rz.uni-ulm.de";
    public static final String URL_OPARU_SIX_REST = "https://bib-test.rz.uni-ulm.de/rest";
    public static final String SSL_VERIFY_OPARU_SIX = "false";
    public static final String COOKIE_KEY_OPARU_SIX = "JSESSIONID";
    public static final int RESPONSE_STATUS_OK_OPARU_SIX = 200;
    public static final String PASSWORD_OPARU_SIX = "Sara+Test"; 
    
       
    /** 
     * OPARU-general config
     */
    public static final String EMAIL_OPARU = "project-sara@uni-konstanz.de";   
    public static final String RESPONSE_REST_TEST_OPARU = "REST api is running.";
    
    /**
     * General config
     */
    public static final String HEADER_CONTENT_TYPE_OPARU = "application/json";
    public static final String HEADER_ACCEPT_TYPE_OPARU = "application/json";
    public static final String RESPONSE_ERROR_JSON = "{\"Error\":\"bad response status\"}";
    public static final String RESPONSE_EMPTY_JSON = "{\"\":\"\"}";
    
    
    
//    public static final String HEADER_CONTENT_TYPE_OPARU = 
//            " {" + 
//            " \"Content-Type\": \"application/" + REQUEST_TYPE_OPARU + "\"" + 
//            " }";
//    public static final String HEADER_ACCEPT_TYPE_OPARU = 
//            " {" + 
//            " \"Accept\": \"application/" + REQUEST_TYPE_OPARU + "\"" + 
//            " }";
//    public static final String HEADER_TOKEN_OPARU = 
//            " {" + 
//            " \"rest-dspace-token\": \"\"" + 
//            " }";


    /** 
     * KOPS config
     * 
     */
    
    
    /**
     * TODO: implement some secure algorithm/mechanism
     *       to get password from the outside.
     * 
     * but for now just return a constant value from this class
     * 
     * @param email
     * @param obj
     * @return password string
     */
    public static String getPassword(String email, PublicationRepository obj){
            
        switch (email){
            case EMAIL_OPARU: 
                if (obj.getRepositoryUrl().equals(URL_OPARU_SIX))
                    return PASSWORD_OPARU_SIX;
                if (obj.getRepositoryUrl().equals(URL_OPARU_FIVE))
                    return PASSWORD_OPARU_FIVE;
            default: 
                return "some_default_password";
        }
    }
    
}
