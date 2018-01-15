package bwfdm.sara.publication.db;

import java.util.UUID;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EPersonDAO extends DAOImpl {
    public final UUID uuid;
    public String contact_email;
    public String password; 
    public Date last_active;
    
	public static String TABLE = "EPerson";
	public static List<String> FIELDS = Arrays.asList("uuid", "contact_email", "password", "last_active");

    public EPersonDAO() {
    	this.uuid = null; 
    	this.contact_email = null; 
    	this.password = null; this.last_active = null;
    }
}
