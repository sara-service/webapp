package bwfdm.sara.publication.test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import bwfdm.sara.publication.PublicationDatabase;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.db.ArchiveDAO;
import bwfdm.sara.publication.db.CollectionDAO;
import bwfdm.sara.publication.db.EPersonDAO;
import bwfdm.sara.publication.db.ItemDAO;
import bwfdm.sara.publication.db.MetadataMappingDAO;
import bwfdm.sara.publication.db.MetadataValueDAO;
import bwfdm.sara.publication.db.RepositoryDAO;
import bwfdm.sara.publication.db.SourceDAO;

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

		SourceDAO mySource = pdb.getList(SourceDAO.class).get(0);
		ArchiveDAO myArchive = pdb.getList(ArchiveDAO.class).get(0);
		RepositoryDAO myRepository = pdb.getList(RepositoryDAO.class).get(0);

		System.out.println("Create an eperson");

		EPersonDAO myEPerson = new EPersonDAO();
		myEPerson.contact_email = "stefan.kombrink@uni-ulm.de";
		myEPerson.password = null;
		myEPerson.last_active = new Date(); // set current time
		myEPerson = (EPersonDAO) pdb.insertInDB(myEPerson);

		System.out.println("Create an item");

		ItemDAO myItem = new ItemDAO();
		myItem.source_uuid = mySource.uuid;
		myItem.archive_uuid = myArchive.uuid;
		myItem.repository_uuid = myRepository.uuid;
		myItem.eperson_uuid = myEPerson.uuid;
		myItem.in_archive = false;
		myItem.item_state = "created";
		myItem.email_verified = true;
		myItem.set("date_created", new Date());
		myItem.date_last_modified = myItem.date_created;
		myItem.set("item_type", "publication");

		myItem = (ItemDAO) pdb.insertInDB(myItem);

		System.out.println("Attach metadata");

		PublicationRepository oparu = pdb.newPublicationRepository(myRepository);

		List<MetadataMappingDAO> metadataMappings = pdb
				.getList(MetadataMappingDAO.class);

		MetadataValueDAO m = new MetadataValueDAO();

		m.item_uuid = myItem.uuid;
		m.data = "my important research project";
		m.map_from = "dc.title";
		m.metadatamapping_uuid = null;

		for (MetadataMappingDAO mm : metadataMappings) {
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

		for (MetadataMappingDAO mm : metadataMappings) {
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

		System.out.println("Repository " + oparu.getDAO().display_name + " offers the following collections: ");

		List<CollectionDAO> myCollections = pdb.getList(CollectionDAO.class);

		boolean noCollection = true;
		for (CollectionDAO c : myCollections) {
			if (c.enabled && c.id == myRepository.uuid) {
				System.out.println(
						"Collection '" + oparu.getCollectionName(c.foreign_collection_uuid) + "' (" + c.foreign_collection_uuid + ")");
				noCollection = false;
			}
		}

		if (noCollection) {
			System.out.println("Querying...");
			Map<String, String> myColls = oparu.getAvailableCollections();

			for (Map.Entry<String, String> entry : myColls.entrySet()) {
				final String map_from = entry.getKey();
				final String map_to = entry.getValue();
				System.out.println("Collection '" + map_to + "' (" + map_from + ")");
			}
		}

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