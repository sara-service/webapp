package bwfdm.sara.publication.db;

import java.util.UUID;

/* default read-only DAO, can be used for most SARA DB tables */

public class CollectionDAO {
	public final UUID id;
	public final String foreign_uuid;
	public final Boolean enabled;

	public CollectionDAO(UUID id, String foreign_uuid, Boolean enabled) {
		this.id = id;
		this.foreign_uuid = foreign_uuid;
		this.enabled = enabled;
	}

	public void dump() {
		System.out.println("ID (Reference to Repository)=" + id.toString());
		System.out.println("foreign_uuid=" + foreign_uuid);
		System.out.println("enabled=" + enabled.toString());
	}
}
