package bwfdm.sara.publication.test;


import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.hibernate.validator.internal.util.privilegedactions.GetConstraintValidatorList;

import bwfdm.sara.publication.dspace.DSpace_v6;
import bwfdm.sara.utils.JsonUtils;

public class DSpaceTest_v6 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String output = "";
		
		//URL 
		String serviceDocumentURL = "http://134.60.51.65:8080/swordv2/servicedocument";
		String restURL = "http://134.60.51.65:8080/rest";
		
		//SARA credentials
		String saraUser = "project-sara@uni-konstanz.de";
		String saraPassword = "SaraTest";
		
		//Test user
		String userLogin = "volodymyr.kushnarenko@uni-ulm.de";

		//Collections
		Map<String, String> saraCollections = new HashMap<String, String>();				
		Map<String, String> userCollections = new HashMap<String, String>();				
		
		//Communities
		List<String> communities;
		
		//Default collection
		String defaultCollectionTitle = "Amokläufe";
		String defaultCollectionIRI = "http://134.60.51.65:8080/swordv2/collection/123456789/40";
		
				
		System.out.println("=== DSpace_v6 testing ===");
				
		// Create DSpace_v6 repository
		DSpace_v6 dspaceRepository = new DSpace_v6(serviceDocumentURL, restURL, saraUser, saraPassword);
//		
//		// Check if repository is accessible -- OK
//		output += "Is repo accessible: " + dspaceRepository.isAccessible() +"\n";
//		
//		// Check if the user is registered -- OK
//		output += "Is \"" + userLogin + "\" " + "registered: " + dspaceRepository.isUserRegistered(userLogin) + "\n";
//		
//		// Check if the user is allowed to publish in the repository -- OK
//		output += "Is \"" + userLogin + "\" assigned:" + dspaceRepository.isUserAssigned(userLogin) + "\n";
//			
//		// Get all available collection titles for the user
//		userCollections = dspaceRepository.getUserAvailableCollectionsWithTitle(userLogin);
//		output += "User available collections: " + userCollections + "\n";
//		for(String key: userCollections.keySet()) {
//			output += "-- " + userCollections.get(key) + "\n";
//		}
//		
//		// Get general available collection titles (for SARA-User)
//		saraCollections = dspaceRepository.getSaraAvailableCollectionsWithTitle();
//		output += "SARA available collections: " + saraCollections + "\n";
//		for(String key: saraCollections.keySet()) {
//			output += "-- " + key + " : " + saraCollections.get(key) + "\n";
//		}
//		
//		
//		
//		// Get communities for every collection
//		for(Map.Entry<String, String> collection: dspaceRepository.getSaraAvailableCollectionsWithTitle().entrySet()) {
//			output += "Communities of the collection \"" + collection.getValue() + "\"\n" ;
//			communities = dspaceRepository.getCommunitiesForCollection(collection.getKey());
//			for(String community: communities) {
//				output += "-- " + community + "\n";
//			}
//		}
//		
//		
//		// Get collection handle based on the URL (for SARA-user)
//		for(Map.Entry<String, String> collection: dspaceRepository.getSaraAvailableCollectionsWithTitle().entrySet()) {
//			output += "Handle of the collection \"" + collection.getValue() + "\"" + "\n";
//			output += "-- URL:  " + collection.getKey() + 
//							   " -> Handle: " + dspaceRepository.getCollectionHandle(collection.getKey()) + "\n";
//		}
//		
//		
		// Get user available collectons with full names
		output += "\n" + "== User available colections with full name \n";
		for(Map.Entry<String, String> collection: dspaceRepository.getUserAvailableCollectionsWithFullName(userLogin, "//").entrySet()) {
			output += collection.getValue() + "\n";
			output += "-- URL:  " + collection.getKey() + 
							   " -> Handle: " + dspaceRepository.getCollectionHandle(collection.getKey()) + "\n";
		}
//		
//		
//		// Get SARA available collectons with full names
//		output += "\n" + "== SARA available colections with full name \n";
//		for(Map.Entry<String, String> collection: dspaceRepository.getSaraAvailableCollectionsWithFullName("//").entrySet()) {
//			output += collection.getValue() + "\n";
//			output += "-- URL:  " + collection.getKey() + 
//							   " -> Handle: " + dspaceRepository.getCollectionHandle(collection.getKey()) + "\n";
//		}
		
		
		// Publication collection
		String publicationCollectionURL = "http://134.60.51.65:8080/swordv2/collection/123456789/36";
		// ZIP-Archive
		File file = new File("D:/tmp_dspace_publication/package_with_metadata.zip");
		// Metadata
		Map<String, String> metadataMap = new HashMap<String, String>();
		metadataMap.put("title", "My title !!!"); //OK, accepted
		metadataMap.put("not-real-field", "unreal-name"); //will not be accepted
		metadataMap.put("publisher", "some publisher"); //OK, accepted
		metadataMap.put("author", "author-1"); //will not be accepted
		//metadataMap.put("dc.contributor.author", "author-2"); 
		
		
//		// Test publication of file
//		output += "\n" + "== PUBLICATION test: ZIP-Archive only ==\n";
//		dspaceRepository.publishElementZipArchiveOnly(userLogin, publicationCollectionURL, file);
		
		
//		// Test publication of metadata
//		output += "\n" + "== PUBLICATION test: METADATA only ==\n";
//		dspaceRepository.publishElementMetadataOnly(userLogin, publicationCollectionURL, metadataMap);
		
		
		// Test publication of file + metadata
		output += "\n" + "== PUBLICATION test: ZIP-Archive + METADATA ==\n";
		dspaceRepository.publishElementZipArchiveAndMetadata(userLogin, publicationCollectionURL, file, metadataMap);
		
		//TODO: play with "entry.xml" -- title from there is other title, it can overwrite a title in DSpace. 
		//      Sword-client title can not!
		
		
		System.out.println(output);
		
		
		
//		// Get SARA-User available collection full names
//		saraCollections = dspaceRepository.getAvailableCollectionFullNames();
//		System.out.println("SARA available collections with full name: " + saraCollections);
//		for(String key: saraCollections.keySet()) {
//			System.out.println("-- " + key + " : " + saraCollections.get(key));
//		}
		
		
				
//		// Get collection name based on IRI (for DSpace it is "WorkspaceName + CollectionName")
//		collections = dspaceRepository.getUserAvailableCollections(userLogin);
//		for(Map.Entry<String, String> e: collections.entrySet()) {
//			if(e.getValue().equals("SARA_collection_normal")) {
//				System.out.println("Collection found: " + e.getValue());
//			}
//		}
		
		
		//System.out.println("Available collections for the user \"" + userLogin + "\":" + dspaceRepository.getUserAvailableCollections(userLogin));
		
		
		
	}

}
