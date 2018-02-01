package bwfdm.sara.publication.db;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("eperson")
public class EPersonDAO extends DAOImpl {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public String contact_email;
	@DatabaseField
	public String password;
	@DatabaseField
	public Date last_active;

	public EPersonDAO(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}
	public EPersonDAO() {
		this.uuid = null;
	}
}
