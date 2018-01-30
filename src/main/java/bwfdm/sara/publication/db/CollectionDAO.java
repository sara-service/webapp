package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@TableName("collection")
public class CollectionDAO extends DAOImpl {
	@PrimaryKey
	public final UUID id;
	@PrimaryKey
	public final String foreign_uuid;
	@PrimaryKey // FIXME why is this part of the primary key???
	public final String display_name;
	public Boolean enabled;

	public static List<String> FIELDS = Arrays.asList("id", "foreign_uuid", "display_name", "enabled");

	public CollectionDAO() {
		id = null;
		foreign_uuid = null;
		display_name = null;
		enabled = null;
	}
}
