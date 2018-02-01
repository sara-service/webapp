package bwfdm.sara.publication.db;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("source")
public class SourceDAO extends DAOImpl {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public String display_name;
	@DatabaseField
	public String url;
	@DatabaseField
	public String contact_email;
	@DatabaseField
	public String adapter;
	@DatabaseField
	public String logo_base64;
	@DatabaseField
	public Boolean enabled;

	public SourceDAO(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public SourceDAO() {
		uuid = null;
	}
}
