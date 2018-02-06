package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("item")
public class ItemDAO implements DAO {
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
	@DatabaseField
	public Date date_created;
	@DatabaseField
	public Date date_last_modified;
	@DatabaseField
	public String item_type;
	@DatabaseField
	public String item_state;
	@DatabaseField
	public String foreign_collection_uuid;
	@DatabaseField
	public String foreign_item_uuid;
	@DatabaseField
	public String persistent_identifier;
	@DatabaseField
	public Boolean email_verified;
	@DatabaseField
	public Boolean in_archive;

	public ItemDAO(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public ItemDAO() {
		uuid = null;
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

	public boolean isVerified() {
		return email_verified;
	}

}