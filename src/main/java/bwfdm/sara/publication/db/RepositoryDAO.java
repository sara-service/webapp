package bwfdm.sara.publication.db;

/* default read-only DAO, can be used for most SARA DB tables */

public class RepositoryDAO {
	
	public final String name;
    public final String URL;
    public final String query_API_endpoint;  // REST query API endpoint
    public final String query_user;          // REST user name (queries only, no submissions)
    public final String query_pwd;           // ...corresponding pwd/key
    public final String submit_API_endpoint; // SWORD API endpoint
    public final String submit_user;         // SWORD user name with submission rights
    public final String submit_pwd;          // ...corresponding pwd/key
    public final String contactEMail;        // an email address to contact the repository
    
    // optionally
	public final String version;             // Show version info for compatibility.
	public final String default_collection;         // submit into an default collection.

    public RepositoryDAO(
    		String n,
    		String v,
    		String u,
    		String quAPIEP,
    		String quUser,
    		String quPwd,
    		String suAPIEP,
    		String suUser,
    		String suPwd,
    		String mailaddr,
    		String defColl
    		) {
    	name = n; URL = u;
    	query_API_endpoint = quAPIEP; query_user = quUser; query_pwd = quPwd;
    	submit_API_endpoint = suAPIEP; submit_user = quUser; submit_pwd = quPwd;
    	contactEMail = mailaddr;
    	version = v; default_collection = defColl;
    }
    
    public void dump() {
    	System.out.println("Name=" + name);
    	System.out.println("URL=" + URL);
    	System.out.println("contactEMail=" + contactEMail);
    }

    

}
