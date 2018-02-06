package bwfdm.sara.publication;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("metadatamapping")
public class MetadataMapping implements DAO {
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

	public MetadataMapping(@JsonProperty("uuid") UUID uuid) {
		this.uuid = uuid;
	}
	public MetadataMapping() {
		uuid = null;
	}
}
