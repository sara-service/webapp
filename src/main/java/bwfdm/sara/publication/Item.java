package bwfdm.sara.publication;

/**
 * @author sk
 */

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.project.Name;
import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("item")
public class Item implements DAO {
	@PrimaryKey
	public final UUID uuid;

	// source stuff
	@DatabaseField
	public UUID source_uuid;
	@DatabaseField
	public String source_user_id;
	@DatabaseField
	public String contact_email;

	// metadata
	@DatabaseField
	public String title;
	@DatabaseField
	public String version;
	@DatabaseField
	public String description;
	@DatabaseField
	public String master;
	@DatabaseField
	public String submitter_surname;
	@DatabaseField
	public String submitter_givenname;

	// archive stuff
	@DatabaseField
	public UUID archive_uuid;
	@DatabaseField
	public String archive_url;
	@DatabaseField
	public boolean is_public;
	@DatabaseField
	public String token;
	@DatabaseField
	public Date date_created;

	// not handled by generic database stuff!
	public List<Name> authors;

	public Item(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public Item() {
		uuid = null;
	}
}