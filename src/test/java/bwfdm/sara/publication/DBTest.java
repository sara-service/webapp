package bwfdm.sara.publication;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author sk
 */

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.PublicationDatabase;

public class DBTest {
	private static PublicationDatabase pdb;

	@BeforeClass
	public static void init() {
		final DataSource ds = new SimpleDriverDataSource(
				new org.postgresql.Driver(),
				"jdbc:postgresql://localhost:5432/test", "test", "test");
		pdb = new PublicationDatabase(ds);
	}

	@Test
	public void testSources() {
		List<Source> mySources = pdb.getList(Source.class);
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

	@Test
	public void testArchives() {
		List<Archive> myArchives = pdb.getList(Archive.class);
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

	@Test
	public void testRepositories() {
		List<Repository> myRepositories = pdb.getList(Repository.class);
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

			/*
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
			*/

			System.out.println("Configured Metadata Mappings");
			List<MetadataMapping> mms = pdb
					.getList(MetadataMapping.class);
			for (MetadataMapping mm : mms) {
				if (mm.repository_uuid.equals(ir.getDAO().uuid)) {
					//System.out.print("Mapping " + mm.display_name + " from " + mm.map_from + " to " + mm.map_to
					//		+ ", Name in IR = " + ir.getMetadataName(mm.map_to));
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

	@Test
	public void testItems() {
		List<Item> myItems = pdb.getList(Item.class);
		// publication items
		System.out.println("===ITEMS===");
		System.out.println("#Items:" + myItems.size());
		for (Item i : myItems) {
			dump(i);
			i.item_state = ItemState.VERIFIED.name();
			pdb.updateInDB(i);
			dump(i);
			i = pdb.updateFromDB(i);

			// this should raise an exception
			i.archive_uuid = i.repository_uuid;
			try {
				pdb.updateInDB(i);
				fail("WARNING! The schema lacks foreign key constraints!");
			} catch (DataAccessException ex) {
				System.out.println(
						"GOOD! Exception when FK constraint is violated!");
				System.out.println(ex.getMessage());
			}
			i = pdb.updateFromDB(i);
			System.out.println("ItemDAO exists in DB : " + pdb.exists(i));
			i = new Item(
					UUID.fromString("ff2a1271-b84b-4ac9-a07e-5f2419909e97"));
			assertFalse("nonexistent ItemDAO exists in DB", pdb.exists(i));
			try {
				pdb.deleteFromDB(i);
				fail("we have DELETE rights on items table!");
			} catch (DataAccessException ex) {
				System.out.println("GOOD! The item table grants no DELETE rights!");
				System.out.println(ex.getMessage());
			}
		}
	};

	private void dump(DAO d) {
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
}
