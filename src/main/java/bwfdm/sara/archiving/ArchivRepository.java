/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.archiving;

/**
 * Interface for the archiv repository.
 * 
 * Idea - allow to connect different types of repositories in the future.
 * For the beginning must be implemented connection to the GitLab 
 * (central repository in Uni Konstanz).
 * 
 * @author vk
 */
public interface ArchivRepository {
    
    public boolean push();
    public boolean pull();
    public String getLinkHistory();
    public String getLinkCommit();
    
}
