package bwfdm.sara.publication.db;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import bwfdm.sara.db.PublicationDatabase;
import bwfdm.sara.publication.PublicationRepositoryFactory;

class Test {
    public static void main(String[] args) {
        System.out.println("SARA-DB Test Program!"); // Display the string.
        final DataSource ds = new SimpleDriverDataSource(
        		new org.postgresql.Driver(), 
        		"jdbc:postgresql://localhost:5432/test", 
        		"test", 
        		"test");
        
        PublicationDatabase pdb= new PublicationDatabase(ds);
        
        List<PublicationRepositoryFactory> myIRs = pdb.getRepositoryFactoryList();
        List<ArchiveDAO> myArchives = pdb.getArchiveList();
        List<SourceDAO> mySources = pdb.getSourceList();
        List<ItemDAO> myItems = pdb.getItemList();
        
        System.out.println("===REPOSITORIES===");
        System.out.println( "#IRs: " + myIRs.size() );
        
        // institutional repositories
        for (PublicationRepositoryFactory ir : myIRs ) {
        	pdb.newPubRepo(ir.dao.uuid.toString()).dump();
        }
        
        // git archives
        System.out.println("===ARCHIVES===");
        System.out.println("#Archives:" + myArchives.size());
        for (ArchiveDAO a : myArchives ) {
        	a.dump();
        }
        
        // git sources
        System.out.println("===SOURCES===");
        System.out.println("#Sources:" + mySources.size());
        for (SourceDAO s : mySources ) {
        	s.dump();
        }
        
        // publication items
        System.out.println("===ITEMS===");
        System.out.println("#Items:" + myItems.size());
        for (ItemDAO i : myItems ) {
        	i.dump();
        }

    }
}