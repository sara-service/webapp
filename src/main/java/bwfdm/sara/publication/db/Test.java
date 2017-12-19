package bwfdm.sara.publication.db;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import bwfdm.sara.db.PublicationDatabase;
import bwfdm.sara.publication.PublicationRepositoryFactory;
import jersey.repackaged.com.google.common.collect.Maps;
import bwfdm.sara.publication.PublicationRepository;

class Test {
	
	static PublicationDatabase pdb;
	
	static List<DAO> myPersons, mySources, myArchives, myRepositories, myItems;
	
	public static void testPersons() {
		System.out.println("===PERSONS===");
		System.out.println("#Sources:" + myPersons.size());
		for (DAO p : myPersons ) {
			p.dump();
			pdb.updateFromDB(p);
			p.set("contact_email", "Tri Tra Trullalala der Kasper der ist wieder da");
			pdb.updateInDB(p);
		}
	}
	
	public static void testSources() {
        // git sources
        System.out.println("===SOURCES===");
        System.out.println("#Sources:" + mySources.size());
        for (DAO s : mySources ) {
        	s.dump();
        	pdb.updateFromDB(s);
        	s.set("display_name", "Kasperletheater");
        	// not allowed as of permissions.sql
        	//pdb.updateInDB(s);
        }
	}
	
	public static void testArchives() {        
        // git archives
        System.out.println("===ARCHIVES===");
        System.out.println("#Archives:" + myArchives.size());
        for (DAO a : myArchives ) {
        	a.dump();
        	pdb.updateFromDB(a);
        	a.set("display_name", "Kasperletheater");
        	// not allowed as of permissions.sql
        	//pdb.updateInDB(a);
        }
    };
        
    public static void testRepositories() {
		// institutional repositories
        System.out.println("===REPOSITORIES===");
        System.out.println( "#IRs: " + myRepositories.size() );
    	for (DAO r: myRepositories) {
    		r.dump();
    		pdb.updateFromDB(r);
    		r.set("display_name", "Kasperletheater");
    		
			PublicationRepository ir = pdb.newPublicationRepository((RepositoryDAO)r);
			ir.dump();
        	// not allowed as of permissions.sql
        	//pdb.updateInDB(r);
    	}
    }
	/*
	public static void testPubRepos() {        
		// institutional repositories
        System.out.println("===REPOSITORIES===");
        System.out.println( "#IRs: " + myIRs.size() );
        for (PublicationRepositoryFactory ir : myIRs ) {
        	PublicationRepository IR = pdb.newPubRepo(ir.dao.uuid.toString());
        	System.out.println("-------- "+ ir.dao.display_name + "-------");
        	
        	System.out.println("Configured Collections");
        	List<CollectionDAO> colls = pdb.getCollectionList(ir.dao.uuid);
        	for (CollectionDAO coll : colls ) {
        		System.out.print("UUID=" + coll.foreign_uuid);
        		System.out.print(" Name=" + IR.getCollectionName(coll.foreign_uuid));
        		if (coll.enabled) {
        			System.out.println(" ENABLED");
        		} else {
        			System.out.println(" DISABLED");
        		}
        	}
        	
        	System.out.println("Configured Metadata Mappings");
        	List<MetadataDAO> mms = pdb.getMetadataList(ir.dao.uuid);
        	for (MetadataDAO mm : mms ) {
        		System.out.print("Mapping from " + mm.map_from + " to " + mm.map_to + " Name=" + IR.getMetadataName(mm.map_to));
        		if (mm.enabled) {
        			System.out.println(" ENABLED");
        		} else {
        			System.out.println(" DISABLED");
        		}
        	}
        	
        	if (!IR.isAccessible()) {
    			System.out.println("Access NOT possible!");
    			IR.dump();
        	}
        	else {
        		System.out.println("Access possible!");
       			System.out.println("Retrieved collections: " + IR.getAvailableCollections());
        		final String user_email = "stefan.kombrink@uni-ulm.de";
        		System.out.println("Heuristically checking whether user mail and repository match...");
        		if (IR.isUserAssigned(user_email)) {
        			System.out.println(user_email + " is assigned to this IR");
        		} else {
        			System.out.println(user_email + " is NOT assigned to this IR");
        		}
        		System.out.println("Validating whether user is registered to this repository...");
        		if (!IR.isUserRegistered(user_email)) {
        			System.out.println("User is NOT registered. Items cannot be submitted on behalf of him/her");
        		} else {
        			System.out.println("User is registered. Items can be submitted on behalf of him/her. Trying...");
        			ItemDAO i = myItems.get(0);
        			IR.publishItem(pdb.withEMailVerified(i, true));
        		}
        	}
        	
        }};
	*/
	public static void testItems() {        
		// publication items
        System.out.println("===ITEMS===");
        System.out.println("#Items:" + myItems.size());
        for (DAO i : myItems ) {
        	i.set("email_verified", true);
        	pdb.updateInDB(i);
        	i.dump();
        	i=(ItemDAO) pdb.updateFromDB(i);
        }};
	
    public static void main(String[] args) {
        System.out.println("SARA-DB Test Program!"); // Display the string.
        
        final DataSource ds = new SimpleDriverDataSource(
        		new org.postgresql.Driver(), 
        		"jdbc:postgresql://localhost:5432/test", 
        		"test", 
        		"test");
        
        pdb = new PublicationDatabase(ds);
        
        myPersons = pdb.getList(EPersonDAO.TABLE);
        myRepositories = pdb.getList(RepositoryDAO.TABLE);
        myArchives = pdb.getList(ArchiveDAO.TABLE);
        mySources = pdb.getList(SourceDAO.TABLE);
        myItems = pdb.getList(ItemDAO.TABLE);
        
        //testPersons();
        //testSources();
        //testArchives();
        testRepositories();
        //testItems();
    }
}