package bwfdm.sara.publication.db;

import java.util.List;
import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.Driver;
//import java.sql.SQLException;

//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import bwfdm.sara.db.PublicationDatabase;
import bwfdm.sara.publication.db.RepositoryDAO;
//import bwfdm.sara.project.Project;

class Test {
    public static void main(String[] args) {
        System.out.println("SARA-DB Test Program!"); // Display the string.
        final DataSource ds = new SimpleDriverDataSource(
        		new org.postgresql.Driver(), 
        		"jdbc:postgresql://localhost:5432/test", 
        		"test", 
        		"test");
        
        PublicationDatabase pdb= new PublicationDatabase(ds);
        
        List<RepositoryDAO> myIRs = pdb.getRepositoryList();
    	
        System.out.println( "#IRs " + myIRs.size() );
        
        for (RepositoryDAO ir : myIRs ) {
        	ir.dump();
        }

    } 
}