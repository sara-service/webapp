package bwfdm.sara.publication.db;

import bwfdm.sara.publication.db.ItemState;
import bwfdm.sara.publication.db.ItemType;
import java.util.UUID;
import java.util.Date;
//import java.sql.Timestamp;

/* read-only DAO */

public class ItemDAO {
	public final UUID uuid;
	public final UUID submitter_uuid;
	public final UUID archive_uuid;
	public final UUID repository_uuid;
	public Date dateCreated;
	public final Date dateLastModified;
	public final ItemType itemType;
	public ItemState itemState;
	public String foreign_uuid;
	public String citation_handle;
	
	// Item for publication
	public ItemDAO(UUID id, ItemType t, ItemState s, Date crDate, Date lmDate, UUID sRef, UUID pRef, UUID aRef, String fuuid, String doi) {
		uuid = id;
		itemType = t; itemState = s;
		dateCreated = crDate; dateLastModified = lmDate;
		submitter_uuid = sRef; repository_uuid = pRef; archive_uuid = aRef; foreign_uuid = fuuid;
		citation_handle = doi;
	}
	
    public void dump() {
    	System.out.println("UUID=" + uuid.toString());
    	System.out.println("ItemType=" + itemType.toString());
    	System.out.println("ItemState=" + itemState.toString());
    	System.out.println("DateCreated=" + dateCreated.toString());
    }

	public ItemState getStatus() { return itemState; }
	public ItemType getType() { return itemType; }

	public boolean isArchiveOnly() { return itemType == ItemType.archive; }
	public boolean isArchived() { return itemState == ItemState.processed; }
	public boolean isRecorded() { return itemType == ItemType.record && itemState == ItemState.processed; }
	public boolean isPublished() { return itemType == ItemType.publication && itemState == ItemState.processed; }
}
