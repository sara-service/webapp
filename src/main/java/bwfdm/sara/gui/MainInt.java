/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.gui;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.OparuFive;
import bwfdm.sara.publication.OparuSix;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.dspace.DSpaceRest;
import bwfdm.sara.publication.dspace.DSpaceVersionFive;
import bwfdm.sara.publication.dspace.DSpaceVersionSix;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vk
 */
public class MainInt {
    
    //public static OparuFive oparu = new OparuFive();
    
    
    public static DSpaceRest oparu;
    public static DSpaceRest oparuFive = new DSpaceVersionFive(
                                                "OPARU-5", 
                                                DSpaceConfig.URL_OPARU_FIVE, 
                                                DSpaceConfig.URL_OPARU_FIVE_REST, 
                                                DSpaceConfig.SSL_VERIFY_OPARU_FIVE,
                                                DSpaceConfig.RESPONSE_STATUS_OK_OPARU_FIVE);
    public static DSpaceRest oparuSix = new DSpaceVersionSix(
                                                "OPARU-6", 
                                                DSpaceConfig.URL_OPARU_SIX, 
                                                DSpaceConfig.URL_OPARU_SIX_REST, 
                                                DSpaceConfig.SSL_VERIFY_OPARU_SIX,
                                                DSpaceConfig.RESPONSE_STATUS_OK_OPARU_SIX);
    
    public static PublicationRepository pubRepo;
    public static PublicationRepository pubRepoOparuFive = new OparuFive();
    public static PublicationRepository pubRepoOparuSix = new OparuSix();
       
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
                pubRepo = pubRepoOparuSix;
                new TestForm().setVisible(true);
                
            }
        });
    
    }
    
    public static void chooseOparuFive(){
        //oparu = null;
        //oparu = new OparuFive();
        oparu = oparuFive;
        pubRepo = pubRepoOparuFive;
    }
    
    public static void chooseOparuSix(){
        //oparu = null;
        //oparu = new OparuSix();
        oparu = oparuSix;
        pubRepo = pubRepoOparuSix;
    }
    
    public static String testRest(){
        return Boolean.toString(oparu.isRestEnable());
    }
    
    public static String getConnectionStatus(){
        return oparu.getConnectionStatus();
    }
    
    public static String loginDspace(){
        isOK = oparu.login(DSpaceConfig.EMAIL_OPARU, DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU, pubRepo));
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
    
    public static String deleteItem(String id){
        return oparu.deleteItem(id);
    }
    
    public static String addItemMetadata(String id, String metadata){
        return oparu.addItemMetadata(id, metadata);
    }
    
    public static String updateItemMetadata(String id, String metadata){
        return oparu.updateItemMetadata(id, metadata);
    }
    
    public static String clearItemMetadata(String id){
        return oparu.clearItemMetadata(id);
    }
    
    public static String getAllItemBitstreams(String id){
        return oparu.getItemBitstreams(id);
    }
 
    public static String getAllBitstreams(){
        return oparu.getAllBitstreams();
    }
    
    public static String itemAddBitstream(String itemID, String bitstreamDescription){
        return oparu.addItemBitstream(itemID, bitstreamDescription);
    }
    
    public static void findItemsPerMetadataAuthor(){
        
    }
    
    public static void createNewItem(){
        
    }
    
    public static void exit(){
        
    }
    
}
