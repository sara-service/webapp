package bwfdm.sara.publication.test;

import java.util.List;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import bwfdm.sara.publication.PublicationDatabase;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.db.*;

class DBTest {
	
	static PublicationDatabase pdb;
	
	static List<EPersonDAO> myPersons;
	static List<SourceDAO> mySources;
	static List<ArchiveDAO> myArchives;
	static List<RepositoryDAO> myRepositories;
	static List<ItemDAO> myItems;
	
	public static void testPersons() {
		System.out.println("===PERSONS===");
		System.out.println("#Sources:" + myPersons.size());
		for (EPersonDAO p : myPersons ) {
			p.dump();
			pdb.updateFromDB(p);
			p.contact_email="Tri Tra Trullalala der Kasper der ist wieder da";
			pdb.updateInDB(p);
		}
	}
	
	public static void testSources() {
        // git sources
        System.out.println("===SOURCES===");
        System.out.println("#Sources:" + mySources.size());
        for (SourceDAO s : mySources ) {
        	s.dump();
        	pdb.updateFromDB(s);
        	s.display_name="Kasperletheater";
        	// not allowed as of permissions.sql
        	//pdb.updateInDB(s);
        }
	}
	
	public static void testArchives() {        
        // git archives
        System.out.println("===ARCHIVES===");
        System.out.println("#Archives:" + myArchives.size());
        for (ArchiveDAO a : myArchives ) {
        	a.dump();
        	pdb.updateFromDB(a);
        	a.display_name = "Kasperletheater";
        	// not allowed as of permissions.sql
        	//pdb.updateInDB(a);
        }
    };
        
    public static void testRepositories() {
		// institutional repositories
        System.out.println("===REPOSITORIES===");
        System.out.println( "#IRs: " + myRepositories.size() );
    	for (RepositoryDAO r: myRepositories) {
    		r.dump();
    		pdb.updateFromDB(r);
    		r.display_name="Kasperletheater";
    		
			PublicationRepository ir = pdb.newPublicationRepository(r);
			ir.dump();
        	// not allowed as of permissions.sql
        	//pdb.updateInDB(r);
			
			System.out.println("Configured Collections");
			List<CollectionDAO> colls = pdb.getList(CollectionDAO.TABLE);
			for (CollectionDAO coll:colls) {
				System.out.print("UUID=" + coll.foreign_uuid);
				System.out.print(" Name=" + ir.getCollectionName(coll.foreign_uuid));
        		if (coll.enabled) {
        			System.out.println(" ENABLED");
        		} else {
        			System.out.println(" DISABLED");
        		}
			}
			
			System.out.println("Configured Metadata Mappings");
			List<MetadataDAO> mms = pdb.getList(MetadataDAO.TABLE);
			for (MetadataDAO mm:mms) {
				System.out.print("Mapping from " + mm.map_from + " to " + mm.map_to + " Name=" + ir.getMetadataName(mm.map_to));
        		if (mm.enabled) {
        			System.out.println(" ENABLED");
        		} else {
        			System.out.println(" DISABLED");
        		}
			}
			
    	}
    }

	public static void testItems() {        
		// publication items
        System.out.println("===ITEMS===");
        System.out.println("#Items:" + myItems.size());
        for (ItemDAO i : myItems ) {
        	i.dump();
        	i.email_verified = true;
        	pdb.updateInDB(i);
        	i.dump();
        	i=pdb.updateFromDB(i);
        }
    };
	
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
        
        testPersons();
        testSources();
        testArchives();
        testRepositories();
        testItems();
    }
}
