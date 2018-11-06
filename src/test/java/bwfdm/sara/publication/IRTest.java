package bwfdm.sara.publication;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import bwfdm.sara.publication.db.PublicationDatabase;

/**
 * @author sk
 */

public class IRTest {
private static final	String contact_email = "stefan.kombrink@uni-ulm.de";
	private static PublicationDatabase pdb;

	@BeforeClass
	public static void init() {
		final DataSource ds = new SimpleDriverDataSource(
				new org.postgresql.Driver(),
				"jdbc:postgresql://localhost:5432/test", "test", "test");
		pdb = new PublicationDatabase(ds);
	}

	@Test
	public void testCreateItem() {
		System.out.println("SARA-IR Test Program!");

		System.out.println(
				"Select the working gitlab / archive gitlab / institutional repository from DB!");

		Source mySource = pdb.getList(Source.class).get(0);
		Archive myArchive = pdb.getList(Archive.class).get(0);
		Repository myRepository = pdb.getList(Repository.class).get(0);


		System.out.println("Create an item");

		Item myItem = new Item();
		myItem.source_uuid = mySource.uuid;
		myItem.archive_uuid = myArchive.uuid;
		myItem.repository_uuid = myRepository.uuid;
		myItem.contact_email = contact_email;
		myItem.item_state = ItemState.VERIFIED.name();
		myItem.item_state_sent = myItem.item_state;
		myItem.date_created = new Date();
		myItem.date_last_modified = myItem.date_created;
		myItem.item_type = ItemType.PUBLISH.name();
		myItem.source_user_id = "-42";
		myItem.is_public = false;
		myItem.token = "t0k3N";

		myItem = pdb.insertInDB(myItem);
	}

	@Test
	public void testInsertMetadata() {
		System.out.println("Attach metadata");
		Repository myRepository = pdb.getList(Repository.class).get(0);
		PublicationRepository oparu = pdb.newPublicationRepository(myRepository);

		List<MetadataMapping> metadataMappings = pdb
				.getList(MetadataMapping.class);

		Item myItem= pdb.getList(Item.class).get(0);
		MetadataValue m = new MetadataValue(myItem.uuid, null,
				"dc.title");
		m.data = "my important research project";

		for (MetadataMapping mm : metadataMappings) {
			if (m.map_from.equals(mm.map_from)) {
				m.metadatamapping_uuid = mm.uuid;
				/*final String mmname = oparu.getMetadataName(mm.map_to);
				if (mmname != null) {
					System.out.println(
							"Repository " + oparu.getDAO().display_name + " mapping " + mm.map_from + " -> " + mmname);
				} else {
					System.out.println("WARNING: Repository " + oparu.getDAO().display_name
							+ " does not support metadata uuid " + mm.map_to);
				}*/
			}
		}

		pdb.insertInDB(m);

		m.item_uuid = myItem.uuid;
		m.data = "https://archive-gitlab.com/myProjUrl";
		m.map_from = "dc.archive_link";
		m.metadatamapping_uuid = null;

		for (MetadataMapping mm : metadataMappings) {
			if (m.map_from.equals(mm.map_from)) {
				m.metadatamapping_uuid = mm.uuid;
				/*
				final String mmname = oparu.getMetadataName(mm.map_to);
				if (mmname != null) {
					System.out.println(
							"Repository " + oparu.getDAO().display_name + " mapping " + mm.map_from + " -> " + mmname);
				} else {
					System.out.println("WARNING: Repository " + oparu.getDAO().display_name
							+ " does not support metadata uuid " + mm.map_to);
				}
				*/
			}
		}

		pdb.insertInDB(m);

	}

	@Test
	public void testPublicationRepository() {
		Repository myRepository = pdb.getList(Repository.class).get(0);
		PublicationRepository oparu = pdb
				.newPublicationRepository(myRepository);

		String output = "";
		output += "Repository " + oparu.getDAO().display_name + " is accessible: " + oparu.isAccessible();
		output += "\n";
		output += "User is registered: "
				+ oparu.isUserRegistered(contact_email);
		output += "\n";
		output += "User is allowed to publish: "
				+ oparu.isUserAssigned(contact_email);
		output += "\n";
		output += "Repository " + oparu.getDAO().display_name + " offers the following collections: ";
		output += "\n";
		output += "All available collections";
		output += "\n";
		//output += oparu.getAvailableCollectionPaths(">", null);
		output += "\n";
		output += "Collections where the user has access to";
		output += "\n";
		//output += oparu.getAvailableCollectionPaths("=> ", contact_email);
		output += "\n";
		System.out.print("\n" + output);

		// Hierarchy bib = oparu.getHierarchy(contact_email);


		/*
		 * myItem.foreign_collection_uuid = "0815";
		 * 
		 * System.out.println( "Publishing the item to " +
		 * oparu.getDAO().display_name + " collection uuid " +
		 * myItem.foreign_collection_uuid); if (oparu.publishItem(myItem)) {
		 * System.out.
		 * println("Item has been published successfully. Waiting for DOI...");
		 * } else {
		 * System.out.println("There has been an error publishing. Examining..."
		 * ); }
		 */
	}
}