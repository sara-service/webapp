package bwfdm.sara.publication;

/**
 * Interface for the publication repository.
 * 
 * @author sk
 */

import java.util.Map;

import bwfdm.sara.publication.db.ItemDAO;
import bwfdm.sara.publication.db.RepositoryDAO;

public interface PublicationRepository {

	public RepositoryDAO getDAO();

	public Boolean isAccessible();

	public Boolean isUserRegistered(String loginName);

	public Boolean isUserAssigned(String loginName);

	public String getCollectionName(String uuid);

	public String getMetadataName(String uuid);

	public Map<String, String> getAvailableCollections();

	public Boolean publishItem(ItemDAO item);

	public void dump();
}
