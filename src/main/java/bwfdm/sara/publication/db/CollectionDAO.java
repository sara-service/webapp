package bwfdm.sara.publication.db;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("collection")
public class CollectionDAO extends DAOImpl {
	@PrimaryKey
	public final UUID id;
	@PrimaryKey
	public final String foreign_collection_uuid;
	@PrimaryKey // FIXME why is this part of the primary key???
	public final String display_name;
	@DatabaseField
	public boolean enabled;

	public CollectionDAO(@JsonProperty("id") UUID id,
			@JsonProperty("foreign_collection_uuid") String foreign_collection_uuid,
			@JsonProperty("display_name") String display_name) {
		this.id = id;
		this.foreign_collection_uuid = foreign_collection_uuid;
		this.display_name = display_name;
	}
}
