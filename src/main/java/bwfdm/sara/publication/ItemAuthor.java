package bwfdm.sara.publication;

import java.util.UUID;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DatabaseField;
import bwfdm.sara.publication.db.TableName;

@TableName("item_authors")
public class ItemAuthor implements DAO {
	@DatabaseField
	public UUID item_uuid;
	@DatabaseField
	public int seq;
	@DatabaseField
	public String surname;
	@DatabaseField
	public String givenname;

	public ItemAuthor(final UUID item_uuid, final int seq, final String surname,
			final String givenname) {
		this.item_uuid = item_uuid;
		this.seq = seq;
		this.surname = surname;
		this.givenname = givenname;
	}

	/** used by Jackson when deserializing from a database row */
	public ItemAuthor() {
	}
}