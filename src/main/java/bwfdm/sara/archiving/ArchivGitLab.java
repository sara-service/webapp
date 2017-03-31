/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bwfdm.sara.archiving;

/**
 *
 * @author vk
 */
public class ArchivGitLab implements ArchivRepository{

    private String linkCommit = "https://link.to.magic.commint";
    private String linkHistory = "https://link.to.magic.commint.history";
    
    @Override
    public boolean push() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean pull() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLinkHistory() {
        return linkHistory;
    }

    @Override
    public String getLinkCommit() {
        return linkCommit;
    }
    
}
