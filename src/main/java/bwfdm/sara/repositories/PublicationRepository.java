/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.repositories;

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
 * @author vk
 */
public interface PublicationRepository {
    
    public boolean loginPublicationRepository();
    public boolean logoutPublicationRepository();
    public void publishElement();
    public void changeElement();
    public void deleteElement();
    public void changeElementMetadata();
    
}
