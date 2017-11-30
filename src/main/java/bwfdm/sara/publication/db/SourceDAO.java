package bwfdm.sara.publication.db;

/* default read-only DAO, can be used for most SARA DB tables */

public class SourceDAO {
	
	public final String name;
    public final String URL;
    public final String api_Endpoint;
    // Kram von Matthias ...
    
    // optionally
    public final String oauth_secret;
    
    public SourceDAO(
    		String n,
    		String u,
    		String apie,
    		String s
    		) {
    	name = n; URL = u;
    	api_Endpoint = apie; oauth_secret = s;
    }
    
    public void dump() {
    	System.out.println("Name=" + name);
    	System.out.println("URL=" + URL);
    }
}
