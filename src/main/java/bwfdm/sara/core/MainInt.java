/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.core;

import bwfdm.sara.repositories.DSpace;
import bwfdm.sara.repositories.DSpaceConfig;
import bwfdm.sara.repositories.OparuFive;
import bwfdm.sara.repositories.OparuSix;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vk
 */
public class MainInt {
    
    //public static OparuFive oparu = new OparuFive();
    public static DSpace oparu;
    
    public static DSpace oparuFive = new OparuFive();
    public static DSpace oparuSix = new OparuSix();
   
    
    
    
    public static boolean isOK;
    
   
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Test Form
       /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TestForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TestForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TestForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TestForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                oparu = oparuSix;
                new TestForm().setVisible(true);
                
            }
        });
    
    }
    
    public static void chooseOparuFive(){
        //oparu = null;
        //oparu = new OparuFive();
        oparu = oparuFive;
    }
    
    public static void chooseOparuSix(){
        //oparu = null;
        //oparu = new OparuSix();
        oparu = oparuSix;
    }
    
    public static String testRest(){
        return Boolean.toString(oparu.isRestEnable());
    }
    
    public static String getConnectionStatus(){
        return oparu.getConnectionStatus();
    }
    
    public static String loginDspace(){
        isOK = oparu.login(DSpaceConfig.EMAIL_OPARU, DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU, oparu));
        String str = "login OK: " + isOK + "\n"; 
//                +
//                     "token login: " + oparu.getToken();
        return str;
    }
    
    public static String logoutDspace(){
        isOK = oparu.logout();
        String str = "logout OK: " + isOK + "\n"; 
//        +
//                     "token logout: " + oparu.getToken();
        return str;
    }
    
    public static String getAllCommunities(){
        return oparu.getAllCommunities();
    }
    
    public static String getCommunityById(String id){
        return oparu.getCommunityById(id);
    }
    
    public static String createCommunity(String name, String idParent){
        return oparu.createCommunity(name, idParent);
    }
    
    public static String deleteCommunity(String idCommunity){
        return oparu.deleteCommunity(idCommunity);
    }
    
    public static String updateCommunity(String newName, String idCommunity){
        return oparu.updateCommunity(newName, idCommunity);
    }
    
    public static String getAllCollections(){
        return oparu.getAllCollections();
    }
    
    public static String getCollectionById(String id){
        return oparu.getCollectionById(id);
    }
    
    public static String createCollection(String name, String idParent){
        return oparu.createCollection(name, idParent);
    }
    
    public static String deleteCollection(String idCollection){
        return oparu.deleteCollection(idCollection);
    }
    
    public static String updateCollection(String newName, String idCollection){
        return oparu.updateCollection(newName, idCollection);
    }
    
    public static String getAllItems(){
        return oparu.getAllItems();
    }
    
    public static String getItemById(String id){
        return oparu.getItemById(id);
    }
    
    public static String getItemMetadataById(String id){
        return oparu.getItemMetadataById(id);
    }
    
    public static String createItem(String name, String itemTitel, String idCollection){
        return oparu.createItem(name, itemTitel, idCollection);
    }
    
    public static String addItemMetadata(String id, String metadata){
        return oparu.itemAddMetadata(id, metadata);
    }
 
    
    public static void findItemsPerMetadataAuthor(){
        
    }
    
    public static void createNewItem(){
        
    }
    
    public static void exit(){
        
    }
    
}
