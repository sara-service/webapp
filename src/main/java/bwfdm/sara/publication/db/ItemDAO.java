package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jersey.repackaged.com.google.common.collect.Lists; // TODO get rid of it!!!

public class ItemDAO extends DAOImpl {
	public final UUID uuid;
	public final UUID eperson_uuid;
	public final UUID source_uuid;
	public final UUID archive_uuid;
	public UUID repository_uuid;
	public final Date date_created;
	public Date date_last_modified;
	public final String item_type;
	public String item_state;
	public String foreign_uuid;
	public String citation_handle;
	public Boolean email_verified;
	public Boolean in_archive;

	public static String TABLE = "Item";
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

	public List<String> getDynamicFieldNames() {
		List<String> fn = Lists.newArrayList();
		fn.clear();
		List<String> dyn_fn = super.getDynamicFieldNames();
		for (String s : FIELDS) {
			if (dyn_fn.contains(s)) {
				fn.add(s);
			} else {
				System.out.println("WARNING! " + s + " is used in FIELDS but not declared as member! Skipping...");
			}
		}
		return fn;
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