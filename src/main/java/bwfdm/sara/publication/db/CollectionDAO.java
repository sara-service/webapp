package bwfdm.sara.publication.db;

import java.util.UUID;

@TableName("collection")
public class CollectionDAO extends DAOImpl {
	@PrimaryKey
	public final UUID id;
	@PrimaryKey
	public final String foreign_collection_uuid;
	@PrimaryKey // FIXME why is this part of the primary key???
	public final String display_name;
	@DatabaseField
	public Boolean enabled;

	public CollectionDAO() {
		id = null;
		foreign_collection_uuid = null;
		display_name = null;
		enabled = null;
	}
}
