package bwfdm.sara.publication.db;

import java.util.UUID;
import java.util.Date;

/* default read-only DAO, can be used for most SARA DB tables */

public class EPersonDAO {
    public final UUID uuid;
    public final String contact_email;
    public final String password; 
    public final Date last_active;
    
    public EPersonDAO(
    		UUID uuid,
    		String contact_email,
    		String password,
    		Date last_active
    		) {
    	this.uuid = uuid; 
    	this.contact_email = contact_email; 
    	this.password = password; this.last_active = last_active;
    }
    
    public void dump() {
    	System.out.println("UUID=" + uuid.toString());
    	System.out.println("email=" + contact_email);
    	System.out.println("password=" + password);
    	System.out.println("last_active=" + last_active);
    }
}
