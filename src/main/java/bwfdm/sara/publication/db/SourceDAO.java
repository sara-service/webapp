package bwfdm.sara.publication.db;

import java.util.UUID;

/* default read-only DAO, can be used for most SARA DB tables */

public class SourceDAO {
	
	public final UUID uuid;
	public final String name;
    public final String URL;
    public final String api_Endpoint;
    public final String oauth_id;
    public final String oauth_secret;
    
    public SourceDAO(
    		UUID id,
    		String n,
    		String u,
    		String apie,
    		String i,
    		String s
    		) {
    	uuid = id; name = n; URL = u;
    	api_Endpoint = apie; oauth_id = i; oauth_secret = s;
    }
    
    public void dump() {
    	System.out.println("UUID=" + uuid);
    	System.out.println("Name=" + name);
    	System.out.println("URL=" + URL);
    }
}
