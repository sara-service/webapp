package bwfdm.sara.publication;

/**
 * Interface for the publication repository.
 * 
 * @author sk
 */

import java.util.Map;

public interface PublicationRepository {

	public Repository getDAO();

	public Boolean isAccessible();

	public Boolean isUserRegistered(String loginName);

	public Boolean isUserAssigned(String loginName);

	public String getCollectionName(String uuid);

	public String getMetadataName(String uuid);

	public Map<String, String> getAvailableCollections();

	public Boolean publishItem(Item item);

	public void dump();
}
