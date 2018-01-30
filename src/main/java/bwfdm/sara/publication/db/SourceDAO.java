package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

	public static List<String> FIELDS = Arrays.asList("uuid", "display_name", "contact_email", "url", "adapter",
			"logo_base64", "enabled");

	public SourceDAO() {
		uuid = null;
		display_name = null;
		url = null;
		contact_email = null;
		adapter = null;
		logo_base64 = null;
		enabled = null;
	}
}
