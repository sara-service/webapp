package bwfdm.sara.publication;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("eperson")
public class EPerson implements DAO {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public String contact_email;
	@DatabaseField
	public String password;
	@DatabaseField
	public Date last_active;

	public EPerson(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}
	public EPerson() {
		this.uuid = null;
	}
}
