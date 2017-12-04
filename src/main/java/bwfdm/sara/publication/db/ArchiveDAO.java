package bwfdm.sara.publication.db;

/* default read-only DAO, can be used for most SARA DB tables */

public class ArchiveDAO {
	
	public final String name;
    public final String URL;
    // Kram von Matthias ...
    
    // optionally
    // ...
    
    public ArchiveDAO(
    		String n,
    		String u
    		) {
    	name = n; URL = u;
    }
    
    public void dump() {
    	System.out.println("Name=" + name);
    	System.out.println("URL=" + URL);
    }
}
