/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Method is got from the "https://dzone.com/articles/jersey-ignoring-ssl"
 * @return Client client
 * @throws Exception 
 * @author vk
 */
public class WebUtils {

    public static Client IgnoreSSLClient() throws Exception {
    SSLContext sslcontext = SSLContext.getInstance("TLS");
    sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
        @Override
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }

    }}, new java.security.SecureRandom());
    return ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier((s1, s2) -> true).build();
    }
    
    
            // to remove
        // Encoding, it is done automatically! We do not need it.
//        try {
//            email = URLEncoder.encode(email, "ISO-8859-1");
//            password = URLEncoder.encode(password, "ISO-8859-1");
//            System.out.println("email + password encoded: " + email + " " + password);
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(OparuSix.class.getName()).log(Level.SEVERE, null, ex);
//        }
    
    
}
