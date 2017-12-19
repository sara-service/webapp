package bwfdm.sara.publication.db;

import bwfdm.sara.publication.db.ItemState;
import bwfdm.sara.publication.db.ItemType;
import jersey.repackaged.com.google.common.collect.Lists;

import java.util.UUID;

import java.lang.reflect.Field;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
/* read-only DAO */

public class ItemDAO extends DAOImpl {
	public final UUID uuid;
	public final UUID eperson_uuid;
	public final UUID source_uuid;
	public final UUID archive_uuid;
	public final UUID repository_uuid;
	public final Date date_created;
	public final Date date_last_modified;
	public final String item_type;
	public final String item_state;
	public final String foreign_uuid;
	public final String citation_handle;
	public final Boolean email_verified;
	public final Boolean in_archive;
	
	// Item for publication
	/*
	public ItemDAO(UUID id, ItemType t, ItemState s, Date crDate, Date lmDate, UUID pRef, UUID sRef, UUID aRef, UUID rRef, String fuuid, String doi, Boolean v) {
		uuid = Objects.requireNonNull(id);
		item_type = t.name(); item_state = s.name();
		date_created = crDate; date_last_modified = lmDate;
		eperson_uuid = pRef; source_uuid = sRef; archive_uuid = aRef;
		repository_uuid = pRef;  foreign_uuid = fuuid;
		citation_handle = doi;
		email_verified = v;
	}*/
	
	public ItemDAO() {
		uuid = null; item_type = null; item_state = null;
		date_created = null; date_last_modified = null;
		eperson_uuid = null; source_uuid = null; archive_uuid = null;
		repository_uuid = null; foreign_uuid = null;
		citation_handle = null; email_verified = null; in_archive = null;
	}
	/*
    public void dump() {
    	System.out.println("UUID=" + uuid.toString());
    	System.out.println("item_type=" + item_type.toString());
    	System.out.println("item_state=" + item_state.toString());
    	System.out.println("date_created=" + date_created.toString());
    }*/

	public ItemState getStatus() { return ItemState.valueOf(item_state); }
	public ItemType getType() { return ItemType.valueOf(item_type); }
	
	public static String TABLE = "Item"; 
	public static List<String> FIELDS =
			Arrays.asList(
					"uuid", "eperson_uuid", "source_uuid", "archive_uuid", "repository_uuid", 
					"date_created", "date_last_modified", "item_type", "item_state", 
					"foreign_uuid", "citation_handle", "email_verified", "in_archive");
	
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
					
/*
	public static String FIELDS_SELECT() {
		String delim = " ";
		String s = ""; 
		for (String ss : FIELDS) {
			s += delim + ss;
			delim = " ,";
		}
		return s + " ";
	}
	
	public static String FIELDS_UPDATE() {
		String delim = " ";
		String s = "";
		for (String ss : FIELDS) {
			s += delim + ss;
			delim = " = ? , ";
		}
		return s + " ";
	}
	*/
	/*
    public static Object get(Object instance, String fieldName) throws Throwable {
        Field field = instance.getClass().getDeclaredField(fieldName);
        return field.get(instance);
    }*/
/*
	public void set(String fieldName, Object value) {
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, value);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
  */  
	public boolean isArchiveOnly() { return item_type.equals(ItemType.archive.name()); }
	//public boolean isArchived() { return item_state == ItemState.processed.name(); }
	//public boolean isRecorded() { return item_type == ItemType.record && item_state == ItemState.processed; }
	//public boolean isPublished() { return item_type == ItemType.publication && item_state == ItemState.processed; }
	public boolean isVerified() { return email_verified; }

}
