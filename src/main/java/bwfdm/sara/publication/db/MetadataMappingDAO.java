package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@TableName("MetadataMapping")
public class MetadataMappingDAO extends DAOImpl {
	public final UUID uuid;
	public final UUID repository_uuid;
	public String display_name;
	public final String map_from;
	public final String map_to;
	public final String remark;
	public Boolean enabled;

	public static List<String> FIELDS = Arrays.asList("uuid", "repository_uuid", "display_name", "map_from", "map_to",
			"enabled");

	public MetadataMappingDAO() {
		uuid = null;
		repository_uuid = null;
		display_name = null;
		enabled = null;
		map_from = null;
		map_to = null;
		remark = null;
	}
}
