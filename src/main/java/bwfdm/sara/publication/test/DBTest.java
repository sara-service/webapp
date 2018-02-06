package bwfdm.sara.publication.test;

/**
 * @author sk
 */

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import bwfdm.sara.publication.Archive;
import bwfdm.sara.publication.Collection;
import bwfdm.sara.publication.EPerson;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.MetadataMapping;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.Source;
import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.PublicationDatabase;

class DBTest {

	static PublicationDatabase pdb;

	static List<EPerson> myPersons;
	static List<Source> mySources;
	static List<Archive> myArchives;
	static List<Repository> myRepositories;
	static List<Item> myItems;

	public static void testPersons() {
		System.out.println("===PERSONS===");
		System.out.println("#Sources:" + myPersons.size());
		for (EPerson p : myPersons) {
			dump(p);
			p = pdb.updateFromDB(p);
			p.contact_email = "Tri Tra Trullalala der Kasper der ist wieder da";
			pdb.updateInDB(p);
		}
	}

	public static void testSources() {
		// git sources
		System.out.println("===SOURCES===");
		System.out.println("#Sources:" + mySources.size());
		for (Source s : mySources) {
			dump(s);
			s = pdb.updateFromDB(s);
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
		for (Archive a : myArchives) {
			dump(a);
			a = pdb.updateFromDB(a);
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
		for (Repository r : myRepositories) {
			dump(r);
			r = pdb.updateFromDB(r);
			r.display_name = "Kasperletheater";

			PublicationRepository ir = pdb.newPublicationRepository(r);
			ir.dump();
			// not allowed as of permissions.sql
			// pdb.updateInDB(r);

			List<Collection> colls = pdb.getList(Collection.class);
			System.out.println("Configured Collections");
			for (Collection coll : colls) {
				if (coll.id.equals(ir.getDAO().uuid)) {
					System.out.print("UUID=" + coll.foreign_collection_uuid);
					System.out.print(" Name=" + ir.getCollectionName(coll.foreign_collection_uuid));
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
			List<MetadataMapping> mms = pdb
					.getList(MetadataMapping.class);
			for (MetadataMapping mm : mms) {
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
		for (Item i : myItems) {
			dump(i);
			i.email_verified = true;
			pdb.updateInDB(i);
			dump(i);
			i = pdb.updateFromDB(i);

			// this should raise an exception
			i.archive_uuid = i.eperson_uuid;
			try {
				pdb.updateInDB(i);
				System.out.println("WARNING! The schema lacks foreign key constraints!");
			} catch (DataAccessException ex) {
				System.out.println("GOOD! Exception when FK constraint is violated!");
				System.out.println(ex.getMessage());
			}
			i = pdb.updateFromDB(i);
			System.out.println("ItemDAO exists in DB : " + pdb.exists(i));
			i = new Item(
					UUID.fromString("ff2a1271-b84b-4ac9-a07e-5f2419909e97"));
			System.out.println("ItemDAO exists in DB : " + pdb.exists(i));
			try {
				pdb.deleteFromDB(i);
			} catch (DataAccessException ex) {
				System.out.println("GOOD! The item table grants no DELETE rights!");
				System.out.println(ex.getMessage());
			}
		}
	};

	public static void dump(DAO d) {
		System.out.println(d.getClass().getName());
		System.out.println("=========================");
		List<String> fields = PublicationDatabase
				.getDynamicFieldNames(d.getClass());
		fields.addAll(PublicationDatabase.getPrimaryKey(d.getClass()));
		for (String s : fields) {
			System.out
					.println(s + "==" + PublicationDatabase.getField(d, s));
		}

	}

	public static void main(String[] args) {
		System.out.println("SARA-DB Test Program!"); // Display the string.

		final DataSource ds = new SimpleDriverDataSource(new org.postgresql.Driver(),
				"jdbc:postgresql://localhost:5432/test", "test", "test");

		pdb = new PublicationDatabase(ds);

		myPersons = pdb.getList(EPerson.class);
		myRepositories = pdb.getList(Repository.class);
		myArchives = pdb.getList(Archive.class);
		mySources = pdb.getList(Source.class);
		myItems = pdb.getList(Item.class);

		testPersons();
		testSources();
		testArchives();
		testRepositories();
		testItems();
	}
}
