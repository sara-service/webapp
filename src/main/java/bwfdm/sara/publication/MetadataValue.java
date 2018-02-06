package bwfdm.sara.publication;

/**
 * @author sk
 */

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("metadatavalue")
public class MetadataValue implements DAO {
	@PrimaryKey
	public UUID item_uuid;
	@PrimaryKey
	public UUID metadatamapping_uuid;
	@PrimaryKey
	public String map_from;
	@DatabaseField
	public String data;

	public MetadataValue(@JsonProperty("item_uuid") UUID item_uuid,
			@JsonProperty("metadatamapping_uuid") UUID metadatamapping_uuid,
			@JsonProperty("map_from") String map_from) {
		this.item_uuid = item_uuid;
		this.metadatamapping_uuid = metadatamapping_uuid;
		this.map_from = map_from;
	}
}
