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

    public RepositoryDAO(
    		UUID id,
    		String n,    		
    		String u,
    		String mailaddr,
    		String logo,
    		String a,
    		Boolean e
    		) {
    	uuid = id; display_name = n; url = u; contact_email = mailaddr;
    	adapter = a; logo_base64 = logo; enabled = e;
    }
    
    public void dump() {
    	System.out.println("UUID=" + uuid);
    	System.out.println("name=" + display_name);
    	System.out.println("url=" + url);
    	System.out.println("contact_email=" + contact_email);
    	System.out.println("adapter=" + adapter);
    	System.out.println("enabled=" + enabled);
    }
}
