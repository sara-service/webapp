package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@TableName("metadatavalue")
public class MetadataValueDAO extends DAOImpl {
	@PrimaryKey
	public UUID item_uuid;
	@PrimaryKey
	public UUID metadatamapping_uuid;
	@PrimaryKey
	public String map_from;
	public String data;

	public static List<String> FIELDS = Arrays.asList("item_uuid", "metadatamapping_uuid", "map_from", "data");

	public MetadataValueDAO() {
		item_uuid = null;
		metadatamapping_uuid = null;
		map_from = null;
		data = null;
	}
}
