package bwfdm.sara.publication.db;

import java.util.UUID;

/* default read-only DAO, can be used for most SARA DB tables */

public class MetadataDAO {
	public final UUID id;
	public final String display_name;
    public final String map_from;
    public final String map_to;
    public final Boolean enabled; 
    
    public MetadataDAO(
    		UUID id,
    		String display_name,
    		String map_from,
    		String map_to,
    		Boolean enabled
    		) {
    	this.id = id; this.display_name = display_name; this.enabled = enabled;
    	this.map_from = map_from; this.map_to = map_to;
    }
    
    public void dump() {
    	System.out.println("ID (Reference to Repository)=" + id.toString());
    	System.out.println("display_name=" + display_name);
    	System.out.println("enabled=" + enabled.toString());
    	System.out.println("map_from=" + map_from);
    	System.out.println("map_to=" + map_to);
    }
}
