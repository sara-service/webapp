package bwfdm.sara.publication.db;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("repository")
public class RepositoryDAO implements DAO {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public String display_name;
	@DatabaseField
	public String url;
	@DatabaseField
	public String contact_email; // an email address to contact the repository
	@DatabaseField
	public String adapter;
	@DatabaseField
	public String logo_base64;
	@DatabaseField
	public Boolean enabled;

	public RepositoryDAO(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}

	public RepositoryDAO() {
		uuid = null;
	}
}
