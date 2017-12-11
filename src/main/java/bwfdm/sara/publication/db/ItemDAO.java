package bwfdm.sara.publication.db;

import bwfdm.sara.publication.db.ItemState;
import bwfdm.sara.publication.db.ItemType;
import java.util.UUID;
import java.util.Date;
import java.util.Objects;
/* read-only DAO */

public class ItemDAO {
	public final UUID uuid;
	public final UUID eperson_uuid;
	public final UUID source_uuid;
	public final UUID archive_uuid;
	public final UUID repository_uuid;
	public final Date date_created;
	public final Date date_last_modified;
	public final ItemType item_type;
	public final ItemState item_state;
	public final String foreign_uuid;
	public final String citation_handle;
	
	// Item for publication
	public ItemDAO(UUID id, ItemType t, ItemState s, Date crDate, Date lmDate, UUID pRef, UUID sRef, UUID aRef, UUID rRef, String fuuid, String doi) {
		uuid = Objects.requireNonNull(id);
		item_type = t; item_state = s;
		date_created = crDate; date_last_modified = lmDate;
		eperson_uuid = pRef; source_uuid = sRef; archive_uuid = aRef;
		repository_uuid = pRef;  foreign_uuid = fuuid;
		citation_handle = doi;
	}
	
    public void dump() {
    	System.out.println("UUID=" + uuid.toString());
    	System.out.println("item_type=" + item_type.toString());
    	System.out.println("item_state=" + item_state.toString());
    	System.out.println("date_created=" + date_created.toString());
    }

	public ItemState getStatus() { return item_state; }
	public ItemType getType() { return item_type; }

	public boolean isArchiveOnly() { return item_type == ItemType.archive; }
	public boolean isArchived() { return item_state == ItemState.processed; }
	public boolean isRecorded() { return item_type == ItemType.record && item_state == ItemState.processed; }
	public boolean isPublished() { return item_type == ItemType.publication && item_state == ItemState.processed; }
}
