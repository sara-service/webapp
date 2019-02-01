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

@TableName("item_publication")
public class ItemPublication implements DAO {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public UUID item_uuid;

	// stuff entered on publication page
	@DatabaseField
	public UUID repository_uuid;
	@DatabaseField
	public String repository_url;
	@DatabaseField
	public String repository_login_id;
	@DatabaseField
	public String collection_id;

	// stuff populated after triggering submission
	@DatabaseField
	public String item_id;
	@DatabaseField
	public String persistent_identifier;

	// state tracking
	@DatabaseField
	public Date date_created;
	@DatabaseField
	public Date date_last_modified;
	@DatabaseField
	public String item_state;

	public ItemPublication(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public ItemPublication() {
		uuid = null;
	}

	public ItemState getState() {
		return ItemState.valueOf(item_state);
	}
}