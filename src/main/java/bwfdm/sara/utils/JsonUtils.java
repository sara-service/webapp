/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.utils;

import bwfdm.sara.repositories.OparuSix;
import bwfdm.sara.rest.CookieStatusResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vk
 */
public class JsonUtils {
      
    public static String jsonStringPrettyPrint(String jsonString){
        
        if (jsonString.equals("")){
            return "bwfdm.sara.utils.JsonUtils.jsonStringPrettyPrint: empty string.";
        }
        ObjectMapper mapper = new ObjectMapper();
        String prettyJsonString = "";
        try {
            Object jsonObject = mapper.readValue(jsonString, Object.class);      
            prettyJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JsonUtils.class.getName()).log(Level.SEVERE, null, ex); //ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(JsonUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prettyJsonString;
    }
    
    public static <T> T jsonStringToObject(String jsonString, Class<T> type){
        ObjectMapper mapper = new ObjectMapper();
        T obj = null;
        try{
            obj = type.cast(mapper.readValue(jsonString, type));
        }catch (IOException ex) {
            Logger.getLogger(OparuSix.class.getName()).log(Level.SEVERE, null, ex);
        }
        return obj;
    }
    
}
