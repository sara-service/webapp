package bwfdm.sara.publication.db;

import java.util.UUID;

@TableName("repository")
public class RepositoryDAO extends DAOImpl {
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

	public RepositoryDAO() {
		uuid = null;
		display_name = null;
		url = null;
		contact_email = null;
		adapter = null;
		logo_base64 = null;
		enabled = null;
	}
}
