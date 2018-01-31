package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.UUID;

@TableName("metadatavalue")
public class MetadataValueDAO extends DAOImpl {
	@PrimaryKey
	public UUID item_uuid;
	@PrimaryKey
	public UUID metadatamapping_uuid;
	@PrimaryKey
	public String map_from;
	@DatabaseField
	public String data;

	public MetadataValueDAO() {
		item_uuid = null;
		metadatamapping_uuid = null;
		map_from = null;
		data = null;
	}
}
