package bwfdm.sara.publication.db;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import bwfdm.sara.db.PublicationDatabase;
import bwfdm.sara.publication.PublicationRepositoryFactory;
import bwfdm.sara.publication.PublicationRepository;

class Test {
	
	static PublicationDatabase pdb;
	
	static List<EPersonDAO> myPersons;
	static List<SourceDAO> mySources;
	static List<ArchiveDAO> myArchives;
	static List<PublicationRepositoryFactory> myIRs;
	static List<ItemDAO> myItems;
	
	public static void testPersons() {
		System.out.println("===PERSONS===");
		System.out.println("#Sources:" + myPersons.size());
		for (EPersonDAO p : myPersons ) {
			p.dump();
		}
	}
	
	public static void testSources() {
        // git sources
        System.out.println("===SOURCES===");
        System.out.println("#Sources:" + mySources.size());
        for (SourceDAO s : mySources ) {
        	s.dump();
        }
	}
	
	public static void testArchives() {        
        // git archives
        System.out.println("===ARCHIVES===");
        System.out.println("#Archives:" + myArchives.size());
        for (ArchiveDAO a : myArchives ) {
        	a.dump();
        }};
	
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
        			System.out.println("User is registered. Items can be submitted on behalf of him/her");
        		}
        	}
        	
        }};
	
	public static void testItems() {        
		// publication items
        System.out.println("===ITEMS===");
        System.out.println("#Items:" + myItems.size());
        for (ItemDAO i : myItems ) {
        	i.dump();
        }};
	
    public static void main(String[] args) {
        System.out.println("SARA-DB Test Program!"); // Display the string.
        
        final DataSource ds = new SimpleDriverDataSource(
        		new org.postgresql.Driver(), 
        		"jdbc:postgresql://localhost:5432/test", 
        		"test", 
        		"test");
        
        pdb= new PublicationDatabase(ds);
        
        myPersons = pdb.getPersonList();
        myIRs = pdb.getRepositoryFactoryList();
        myArchives = pdb.getArchiveList();
        mySources = pdb.getSourceList();
        myItems = pdb.getItemList();

        testPersons();
        testSources();
        testArchives();
        testItems();
        
        testPubRepos();
    }
}