package bwfdm.sara.publication;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("archive")
public class Archive implements DAO {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public String display_name;
	@DatabaseField
	public String contact_email;
	@DatabaseField
	public String url;
	@DatabaseField
	public String adapter;
	@DatabaseField
	public String logo_base64;
	@DatabaseField
	public Boolean enabled;

	public Archive(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public Archive() {
		uuid = null;
	}
}
