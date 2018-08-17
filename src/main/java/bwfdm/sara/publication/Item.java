package bwfdm.sara.publication;

/**
 * @author sk
 */

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("item")
public class Item implements DAO {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public UUID source_uuid;
	@DatabaseField
	public UUID archive_uuid;
	@DatabaseField
	public UUID repository_uuid;
	@DatabaseField
	public String source_user_id;
	@DatabaseField
	public String contact_email;
	@DatabaseField
	public String collection_id;
	@DatabaseField
	public String item_id;
	@DatabaseField
	public Date date_created;
	@DatabaseField
	public Date date_last_modified;
	@DatabaseField
	public String item_type;
	@DatabaseField
	public String item_state;
	@DatabaseField
	public String item_state_sent;
	@DatabaseField
	public String persistent_identifier;
	@DatabaseField
	public String repository_login_id;
	@DatabaseField
	public String meta_title;
	@DatabaseField
	public String meta_version;
	@DatabaseField
	public String meta_description;
	@DatabaseField
	public String meta_submitter;
	@DatabaseField
	public String archive_url;
	@DatabaseField
	public String repository_url;
	@DatabaseField
	public String token;
	@DatabaseField
	public boolean is_public;

	public Item(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public Item() {
		uuid = null;
	}

	public ItemState getState() {
		return ItemState.valueOf(item_state);
	}

	public ItemType getType() {
		return ItemType.valueOf(item_type);
	}

	public boolean isArchiveOnly() {
		return item_type.equals(ItemType.ARCHIVE_HIDDEN.name());
	}
}