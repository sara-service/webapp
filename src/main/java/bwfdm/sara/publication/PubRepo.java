package bwfdm.sara.publication;

import java.util.List;
import java.util.UUID;

/**
 * Interface for the publication repository.
 * 
 * @author sk
 */
public interface PubRepo {

    public UUID getUUID();

	public List<String> getAvailableCollections();
    public Boolean isUserRegistered(String loginName);
	public Boolean isUserAssigned(String loginName);

	public void dump();
}
