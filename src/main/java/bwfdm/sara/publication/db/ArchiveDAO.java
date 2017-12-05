package bwfdm.sara.publication.db;

import java.util.UUID;

import org.jruby.common.IRubyWarnings.ID;

/* default read-only DAO, can be used for most SARA DB tables */

public class ArchiveDAO {
	
	public final UUID uuid;
	public final String name;
    public final String URL;
    // Kram von Matthias ...
    
    // optionally
    // ...
    
    public ArchiveDAO(
    		UUID id,
    		String n,
    		String u
    		) {
    	uuid = id; name = n; URL = u;
    }
    
    public void dump() {
    	System.out.println("UUID=" + uuid.toString());
    	System.out.println("Name=" + name);
    	System.out.println("URL=" + URL);
    }
}
