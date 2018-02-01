package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	public MetadataValueDAO(@JsonProperty("item_uuid") UUID item_uuid,
			@JsonProperty("metadatamapping_uuid") UUID metadatamapping_uuid,
			@JsonProperty("map_from") String map_from) {
		this.item_uuid = item_uuid;
		this.metadatamapping_uuid = metadatamapping_uuid;
		this.map_from = map_from;
	}
}
