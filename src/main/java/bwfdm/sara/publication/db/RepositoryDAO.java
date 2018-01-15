package bwfdm.sara.publication.db;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RepositoryDAO extends DAOImpl {
	
	public final UUID uuid;
	public String display_name;
    public String url;
    public String contact_email;        // an email address to contact the repository
    public String adapter;
    public String logo_base64;
    public Boolean enabled;
    
    public static String TABLE = "Repository";
    public static List<String> FIELDS = Arrays.asList("uuid", "display_name", "url", "contact_email", "adapter", "logo_base64", "enabled");

    public RepositoryDAO() {
    	uuid = null; display_name = null; url = null; contact_email = null;
    	adapter = null; logo_base64 = null; enabled = null;
    }
}
