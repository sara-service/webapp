package bwfdm.sara.publication;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("collection")
public class Collection implements DAO {
	@PrimaryKey
	public final UUID id;
	@PrimaryKey
	public final String foreign_collection_uuid;
	@DatabaseField
	public String display_name;
	@DatabaseField
	public boolean enabled;

	public Collection(@JsonProperty("id") UUID id,
			@JsonProperty("foreign_collection_uuid") String foreign_collection_uuid,
			@JsonProperty("display_name") String display_name) {
		this.id = id;
		this.foreign_collection_uuid = foreign_collection_uuid;
		this.display_name = display_name;
	}
}
