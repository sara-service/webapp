package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@TableName("item")
public class ItemDAO extends DAOImpl {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public UUID eperson_uuid;
	@DatabaseField
	public UUID source_uuid;
	@DatabaseField
	public UUID archive_uuid;
	@DatabaseField
	public UUID repository_uuid;
	public UUID collection_uuid;
	@DatabaseField
	public final Date date_created;
	@DatabaseField
	public Date date_last_modified;
	@DatabaseField
	public final String item_type;
	@DatabaseField
	public String item_state;
	@DatabaseField
	public String foreign_uuid;
	@DatabaseField
	public String citation_handle;
	@DatabaseField
	public Boolean email_verified;
	@DatabaseField
	public Boolean in_archive;

	public static List<String> FIELDS = Arrays.asList("uuid", "eperson_uuid", "source_uuid", "archive_uuid",
			"repository_uuid", "date_created", "date_last_modified", "item_type", "item_state", "foreign_uuid",
			"citation_handle", "email_verified", "in_archive");

	public ItemDAO() {
		uuid = null;
		item_type = null;
		item_state = null;
		date_created = null;
		date_last_modified = null;
		eperson_uuid = null;
		source_uuid = null;
		archive_uuid = null;
		repository_uuid = null;
		foreign_uuid = null;
		citation_handle = null;
		email_verified = null;
		in_archive = null;
	}

	public ItemState getState() {
		return ItemState.valueOf(item_state);
	}

	public ItemType getType() {
		return ItemType.valueOf(item_type);
	}

	public boolean isArchiveOnly() {
		return item_type.equals(ItemType.archive.name());
	}

	/*
	 * public boolean isArchived() { return item_state ==
	 * ItemState.processed.name(); } public boolean isRecorded() { return item_type
	 * == ItemType.record && item_state == ItemState.processed; } public boolean
	 * isPublished() { return item_type == ItemType.publication && item_state ==
	 * ItemState.processed; }
	 */
	public boolean isVerified() {
		return email_verified;
	}

}