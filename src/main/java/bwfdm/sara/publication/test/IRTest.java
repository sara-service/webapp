package bwfdm.sara.publication.test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import bwfdm.sara.publication.Archive;
import bwfdm.sara.publication.Collection;
import bwfdm.sara.publication.EPerson;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.MetadataMapping;
import bwfdm.sara.publication.MetadataValue;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.Source;
import bwfdm.sara.publication.db.PublicationDatabase;

/**
 * @author sk
 */

class IRTest {

	public static void main(String[] args) {
		System.out.println("SARA-IR Test Program!");

		final DataSource ds = new SimpleDriverDataSource(new org.postgresql.Driver(),
				"jdbc:postgresql://localhost:5432/test", "test", "test");

		PublicationDatabase pdb = new PublicationDatabase(ds);

		System.out.println("Select the working gitlab / archive gitlab / institutional repository from DB!");

		Source mySource = pdb.getList(Source.class).get(0);
		Archive myArchive = pdb.getList(Archive.class).get(0);
		Repository myRepository = pdb.getList(Repository.class).get(0);

		System.out.println("Create an eperson");

		EPerson myEPerson = new EPerson();
		myEPerson.contact_email = "stefan.kombrink@uni-ulm.de";
		myEPerson.password = null;
		myEPerson.last_active = new Date(); // set current time
		myEPerson = pdb.insertInDB(myEPerson);

		System.out.println("Create an item");

		Item myItem = new Item();
		myItem.source_uuid = mySource.uuid;
		myItem.archive_uuid = myArchive.uuid;
		myItem.repository_uuid = myRepository.uuid;
		myItem.eperson_uuid = myEPerson.uuid;
		myItem.in_archive = false;
		myItem.item_state = "created";
		myItem.email_verified = true;
		myItem.date_created = new Date();
		myItem.date_last_modified = myItem.date_created;
		myItem.item_type = "publication";

		myItem = pdb.insertInDB(myItem);

		System.out.println("Attach metadata");

		PublicationRepository oparu = pdb.newPublicationRepository(myRepository);

		List<MetadataMapping> metadataMappings = pdb
				.getList(MetadataMapping.class);

		MetadataValue m = new MetadataValue(myItem.uuid, null,
				"dc.title");
		m.data = "my important research project";

		for (MetadataMapping mm : metadataMappings) {
			if (m.map_from.equals(mm.map_from)) {
				m.metadatamapping_uuid = mm.uuid;
				final String mmname = oparu.getMetadataName(mm.map_to);
				if (mmname != null) {
					System.out.println(
							"Repository " + oparu.getDAO().display_name + " mapping " + mm.map_from + " -> " + mmname);
				} else {
					System.out.println("WARNING: Repository " + oparu.getDAO().display_name
							+ " does not support metadata uuid " + mm.map_to);
				}
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
				final String mmname = oparu.getMetadataName(mm.map_to);
				if (mmname != null) {
					System.out.println(
							"Repository " + oparu.getDAO().display_name + " mapping " + mm.map_from + " -> " + mmname);
				} else {
					System.out.println("WARNING: Repository " + oparu.getDAO().display_name
							+ " does not support metadata uuid " + mm.map_to);
				}
			}
		}

		pdb.insertInDB(m);

		System.out.println("Repository " + oparu.getDAO().display_name + " is accessible: " + oparu.isAccessible());
		System.out.println("User is registered: " + oparu.isUserRegistered(myEPerson.contact_email));
		System.out.println("User is allowed to publish: " + oparu.isUserAssigned(myEPerson.contact_email));
		System.out.println("Repository " + oparu.getDAO().display_name + " offers the following collections: ");
		System.out.println("All available collections");
		System.out.println(oparu.getSaraAvailableCollectionsWithFullName(", "));
		System.out.println("Collections where the user has access to");
		System.out.println(oparu.getUserAvailableCollectionsWithFullName(myEPerson.contact_email, ", "));
		
		/*
		List<Collection> myCollections = pdb.getList(Collection.class);

		boolean noCollection = true;
		for (Collection c : myCollections) {
			if (c.enabled && c.id == myRepository.uuid) {
				System.out.println(
						"Collection '" + oparu.getCollectionName(c.foreign_collection_uuid) + "' (" + c.foreign_collection_uuid + ")");
				noCollection = false;
			}
		}

		
		if (noCollection) {
			System.out.println("Querying...");
			Map<String, String> myColls = oparu.getAvailableCollectionTitles();

			for (Map.Entry<String, String> entry : myColls.entrySet()) {
				final String map_from = entry.getKey();
				final String map_to = entry.getValue();
				System.out.println("Collection '" + map_to + "' (" + map_from + ")");
			}
		}
		*/

		myItem.foreign_collection_uuid = "0815";

		System.out.println(
				"Publishing the item to " + oparu.getDAO().display_name + " collection uuid " + myItem.foreign_collection_uuid);
		if (oparu.publishItem(myItem)) {
			System.out.println("Item has been published successfully. Waiting for DOI...");
		} else {
			System.out.println("There has been an error publishing. Examining...");
		}
	}
}