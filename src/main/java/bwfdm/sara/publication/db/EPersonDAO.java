package bwfdm.sara.publication.db;

import java.util.Date;
import java.util.UUID;

@TableName("eperson")
public class EPersonDAO extends DAOImpl {
	@PrimaryKey
	public final UUID uuid;
	@DatabaseField
	public String contact_email;
	@DatabaseField
	public String password;
	@DatabaseField
	public Date last_active;

	public EPersonDAO() {
		this.uuid = null;
		this.contact_email = null;
		this.password = null;
		this.last_active = null;
	}
}
