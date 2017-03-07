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
    
    public static Oparu oparu = new Oparu();
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
                new TestForm().setVisible(true);
            }
        });
        
                
        
        
        System.out.println("Password is: " + DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU));
    
             
                    
        
    }
    
    public static void testRest(){
         System.out.println("Is REST enable: " + oparu.isRestEnable()); 
    }
    
    public static void getTokenStatus(){
        System.out.println("Token status: " + oparu.getTokenStatus(oparu.getToken()));
    }
    
    public static void loginDspace(){
        isOK = oparu.login(DSpaceConfig.EMAIL_OPARU, DSpaceConfig.getPassword(DSpaceConfig.EMAIL_OPARU));
        System.out.println("login OK: " + isOK);
        System.out.println("token login: " + oparu.getToken());
    }
    
    public static void logoutDspace(){
        isOK = oparu.logout();
        System.out.println("logout OK: " + isOK);
        System.out.println("token logout: " + oparu.getToken());
    }
    
    public static String getAllCommunities(){
        //System.out.println("All communities: " + oparu.getAllCommunities());
        return oparu.getAllCommunities();
    }
    
    public static String getCommunityById(String id){
//        String str = oparu.getCommunityByID(id);
//        System.out.println("Community id " + id + ": " + str);
        return oparu.getCommunityByID(id);
    }
    
    public static String createCommunity(String name, String idParent){
//        String str = oparu.createCommunity(name, idParent);
//        System.out.println("Create new Community:" + str);
        return oparu.createCommunity(name, idParent);
    }
    
    public static void getCollections(){
        
    }
    
    public static void getItems(){
        
    }
    
    public static void getItemId(){
        
    }
    
    public static void getItemIdMetadata(){
        
    }
    
    public static void findItemsPerMetadataAuthor(){
        
    }
    
    public static void createNewItem(){
        
    }
    
    public static void exit(){
        
    }
    
}
