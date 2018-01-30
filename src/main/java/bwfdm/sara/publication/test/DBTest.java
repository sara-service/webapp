package bwfdm.sara.publication.test;

/**
 * @author sk
 */

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import bwfdm.sara.publication.PublicationDatabase;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.db.ArchiveDAO;
import bwfdm.sara.publication.db.CollectionDAO;
import bwfdm.sara.publication.db.EPersonDAO;
import bwfdm.sara.publication.db.ItemDAO;
import bwfdm.sara.publication.db.MetadataMappingDAO;
import bwfdm.sara.publication.db.RepositoryDAO;
import bwfdm.sara.publication.db.SourceDAO;

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
		for (EPersonDAO p : myPersons) {
			p.dump();
			pdb.updateFromDB(p);
			p.contact_email = "Tri Tra Trullalala der Kasper der ist wieder da";
			pdb.updateInDB(p);
		}
	}

	public static void testSources() {
		// git sources
		System.out.println("===SOURCES===");
		System.out.println("#Sources:" + mySources.size());
		for (SourceDAO s : mySources) {
			s.dump();
			pdb.updateFromDB(s);
			s.display_name = "Kasperletheater";
			// not allowed as of permissions.sql
			try {
				pdb.updateInDB(s);
			} catch (DataAccessException ex) {
				System.out.println("GOOD! Exception because permission is not granted to update this table!");
				System.out.println(ex.getMessage());
			}
		}
	}

	public static void testArchives() {
		// git archives
		System.out.println("===ARCHIVES===");
		System.out.println("#Archives:" + myArchives.size());
		for (ArchiveDAO a : myArchives) {
			a.dump();
			pdb.updateFromDB(a);
			a.display_name = "Kasperletheater";
			// not allowed as of permissions.sql
			try {
				pdb.updateInDB(a);
			} catch (DataAccessException ex) {
				System.out.println("GOOD! Exception because permission is not granted to update this table!");
				System.out.println(ex.getMessage());
			}
		}
	};

	public static void testRepositories() {
		// institutional repositories
		System.out.println("===REPOSITORIES===");
		System.out.println("#IRs: " + myRepositories.size());
		for (RepositoryDAO r : myRepositories) {
			r.dump();
			pdb.updateFromDB(r);
			r.display_name = "Kasperletheater";

			PublicationRepository ir = pdb.newPublicationRepository(r);
			ir.dump();
			// not allowed as of permissions.sql
			// pdb.updateInDB(r);

			List<CollectionDAO> colls = pdb.getList(CollectionDAO.class);
			System.out.println("Configured Collections");
			for (CollectionDAO coll : colls) {
				if (coll.id.equals(ir.getDAO().uuid)) {
					System.out.print("UUID=" + coll.foreign_uuid);
					System.out.print(" Name=" + ir.getCollectionName(coll.foreign_uuid));
					if (coll.enabled) {
						System.out.println(" [ENABLED]");
					} else {
						System.out.println(" [DISABLED]");
					}
				} else {
					System.out.println("No metadata for this IR: " + coll.id);
				}
			}

			System.out.println("Configured Metadata Mappings");
			List<MetadataMappingDAO> mms = pdb
					.getList(MetadataMappingDAO.class);
			for (MetadataMappingDAO mm : mms) {
				if (mm.repository_uuid.equals(ir.getDAO().uuid)) {
					System.out.print("Mapping " + mm.display_name + " from " + mm.map_from + " to " + mm.map_to
							+ ", Name in IR = " + ir.getMetadataName(mm.map_to));
					if (mm.enabled) {
						System.out.println(" [ENABLED]");
					} else {
						System.out.println(" [DISABLED]");
					}
				} else {
					System.out.println("No metadata for this IR: " + mm.repository_uuid);
				}
			}

		}
	}

	public static void testItems() {
		// publication items
		System.out.println("===ITEMS===");
		System.out.println("#Items:" + myItems.size());
		for (ItemDAO i : myItems) {
			i.dump();
			i.email_verified = true;
			pdb.updateInDB(i);
			i.dump();
			i = pdb.updateFromDB(i);

			// this should raise an exception
			i.set("archive_uuid", i.eperson_uuid);
			try {
				pdb.updateInDB(i);
				System.out.println("WARNING! The schema lacks foreign key constraints!");
			} catch (DataAccessException ex) {
				System.out.println("GOOD! Exception when FK constraint is violated!");
				System.out.println(ex.getMessage());
			}
			i = pdb.updateFromDB(i);
			System.out.println("ItemDAO exists in DB : " + pdb.exists(i));
			i.set("uuid", UUID.fromString("ff2a1271-b84b-4ac9-a07e-5f2419909e97"));
			System.out.println("ItemDAO exists in DB : " + pdb.exists(i));
			try {
				pdb.deleteFromDB(i);
			} catch (DataAccessException ex) {
				System.out.println("GOOD! The item table grants no DELETE rights!");
				System.out.println(ex.getMessage());
			}
		}
	};

	public static void main(String[] args) {
		System.out.println("SARA-DB Test Program!"); // Display the string.

		final DataSource ds = new SimpleDriverDataSource(new org.postgresql.Driver(),
				"jdbc:postgresql://localhost:5432/test", "test", "test");

		pdb = new PublicationDatabase(ds);

		myPersons = pdb.getList(EPersonDAO.class);
		myRepositories = pdb.getList(RepositoryDAO.class);
		myArchives = pdb.getList(ArchiveDAO.class);
		mySources = pdb.getList(SourceDAO.class);
		myItems = pdb.getList(ItemDAO.class);

		testPersons();
		testSources();
		testArchives();
		testRepositories();
		testItems();
	}
}
