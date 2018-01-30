package bwfdm.sara.publication.db;

import java.util.UUID;

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
	public String logo_base64;
	@DatabaseField
	public Boolean enabled;

	public ArchiveDAO() {
		uuid = null;
		display_name = null;
		url = null;
		contact_email = null;
		adapter = null;
		logo_base64 = null;
		enabled = null;
	}
}
