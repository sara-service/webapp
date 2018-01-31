package bwfdm.sara.publication.db;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("metadatamapping")
public class MetadataMappingDAO extends DAOImpl {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public UUID repository_uuid;
	@DatabaseField
	public String display_name;
	@DatabaseField
	public String map_from;
	@DatabaseField
	public String map_to;
	@DatabaseField
	public String remark;
	@DatabaseField
	public Boolean enabled;

	public MetadataMappingDAO(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}
	public MetadataMappingDAO() {
		uuid = null;
	}
}
