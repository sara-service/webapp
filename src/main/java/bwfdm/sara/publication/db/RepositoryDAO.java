package bwfdm.sara.publication.db;

import java.util.UUID;

/* default read-only DAO, can be used for most SARA DB tables */

public class RepositoryDAO {
	
	public final UUID uuid;
	public final String display_name;
    public final String url;
    public final String contact_email;        // an email address to contact the repository
    public final String adapter;
    public final String logo_base64;
    public final Boolean enabled;

    public RepositoryDAO() {
    	uuid = null; display_name = null; url = null; contact_email = null;
    	adapter = null; logo_base64 = null; enabled = null;
    }
}
