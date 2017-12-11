package bwfdm.sara.publication.db;

import java.util.UUID;

/* default read-only DAO, can be used for most SARA DB tables */

public class ArchiveDAO {
	
	public final UUID uuid;
	public final String display_name;
    public final String url;
    public final String adapter;
    public final Boolean enabled;
    // Kram von Matthias ...
    
    // optionally
    // ...
    
    public ArchiveDAO(
    		UUID id,
    		String n,
    		String u,
    		String a,
    		Boolean e
    		) {
    	uuid = id; display_name = n; url = u; adapter = a; enabled = e; 
    }
    
    public void dump() {
    	System.out.println("UUID=" + uuid.toString());
    	System.out.println("display_name=" + display_name);
    	System.out.println("url=" + url);
    	System.out.println("adapter=" + adapter);
    }
}
