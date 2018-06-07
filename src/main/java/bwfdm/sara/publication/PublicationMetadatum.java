package bwfdm.sara.publication;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.PrimaryKey;
import bwfdm.sara.publication.db.TableName;

@TableName("fe_temp_pubmeta")
public class PublicationMetadatum implements DAO {
	@PrimaryKey
	public final UUID item_uuid;
	@PrimaryKey
	public final String field;
	@DatabaseField
	public String value;

	public PublicationMetadatum(@JsonProperty("item_uuid") UUID item_uuid,
			@JsonProperty("field") String field) {
		this.item_uuid = item_uuid;
		this.field = field;
	}
}
