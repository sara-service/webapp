/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.publication;

/**
 * Interface for the publication repository.
 * 
 * Idea - to have some interface, which can allow to implement/connect 
 * differen publication repositories.
 * 
 * Repositories for the beginning:
 * - OPARU (Uni Ulm)
 * - KOPS (Uni Konstanz)
 * 
 * TODO: implement all communication with the real repositories via these methods. 
 * 
 * @author vk
 */
public interface PublicationRepository {
       
    public boolean loginPublicationRepository();
    public boolean logoutPublicationRepository();
    public boolean publishElement(String publicationLink, String metadata);
    public String changeElement();
    public String deleteElement();
    public String changeElementMetadata();
    public String getRepositoryUrl();
    
}
