package bwfdm.sara.publication.db;

import java.util.UUID;

@TableName("MetadataMapping")
public class MetadataMappingDAO extends DAOImpl {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public final UUID repository_uuid;
	@DatabaseField
	public String display_name;
	@DatabaseField
	public final String map_from;
	@DatabaseField
	public final String map_to;
	public final String remark;
	@DatabaseField
	public Boolean enabled;

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
