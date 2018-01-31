package bwfdm.sara.publication.db;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("archive")
public class ArchiveDAO extends DAOImpl {
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

	public ArchiveDAO(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public ArchiveDAO() {
		uuid = null;
	}
}
