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
		
		// Check if repository is accessible -- OK
		output += "Is repo accessible: " + dspaceRepository.isAccessible() +"\n\n";
		
		// Check if the user is registered -- OK
		output += "Is \"" + userLogin + "\" " + "registered: " + dspaceRepository.isUserRegistered(userLogin) + "\n\n";
		
		// Check if the user is allowed to publish in the repository -- OK
		output += "Is \"" + userLogin + "\" assigned:" + dspaceRepository.isUserAssigned(userLogin) + "\n\n";
			
		
		// Get all available collection titles for the user
		userCollections = dspaceRepository.getUserAvailableCollectionsWithTitle(userLogin);
		output += "\n" + "== User (" + userLogin + ") available collections: " + userCollections + "\n";
		for(String key: userCollections.keySet()) {
			output += "-- " + userCollections.get(key) + "\n";
		}
		
		
		// Get general available collection titles (for SARA-User)
		saraCollections = dspaceRepository.getSaraAvailableCollectionsWithTitle();
		output += "\n" + "== SARA available collections: " + saraCollections + "\n\n";
		for(String key: saraCollections.keySet()) {
			output += "-- " + key + " : " + saraCollections.get(key) + "\n";
		}
				
		
		// Get communities for every SARA-available collection
		for(Map.Entry<String, String> collection: dspaceRepository.getSaraAvailableCollectionsWithTitle().entrySet()) {
			output +=  "\n" + "== Communities of the collection \"" + collection.getValue() + "\"\n" ;
			communities = dspaceRepository.getCommunitiesForCollection(collection.getKey());
			for(String community: communities) {
				output += "-- " + community + "\n";
			}
		}
				
		
		// Get user available collectons with full names
		output += "\n" + "== User available colections with full name \n\n";
		for(Map.Entry<String, String> collection: dspaceRepository.getUserAvailableCollectionsWithFullName(userLogin, "//").entrySet()) {
			output += collection.getValue() + "\n";
			output += "-- URL:  " + collection.getKey() + 
							   " -> Handle: " + dspaceRepository.getCollectionHandle(collection.getKey()) + "\n";
		}
		
		
		// Get SARA available collectons with full names
		output += "\n" + "== SARA available colections with full name \n\n";
		for(Map.Entry<String, String> collection: dspaceRepository.getSaraAvailableCollectionsWithFullName("//").entrySet()) {
			output += collection.getValue() + "\n";
			output += "-- URL:  " + collection.getKey() + 
							   " -> Handle: " + dspaceRepository.getCollectionHandle(collection.getKey()) + "\n";
		}
		
//		System.out.println(output);
//		if(true)
//			return;
		
		
		// Publication collection
		String publicationCollectionURL = "http://134.60.51.65:8080/swordv2/collection/123456789/36";
		
		// File, ZIP-Archive, XML-file
		File zipFile = new File("D:/tmp_dspace_publication/package_with_metadata.zip");
		File xmlFile = new File("D:/tmp_dspace_publication/entry_copy.xml");
		File txtFile = new File("D:/tmp_dspace_publication/test-file.TXT");
		File otherFile = new File("D:/tmp_dspace_publication/test-file.with.dots.txt.T");
		File complicatedFile = new File("D:/tmp_dspace_publication/test-file.with.dots.txt-copy.zip");
		
		// Metadata
		Map<String, String> metadataMap = new HashMap<String, String>();
		metadataMap.put("title", "My title !!!"); 			//OK, accepted
		metadataMap.put("not-real-field", "unreal-name"); 	//will not be accepted
		metadataMap.put("publisher", "some publisher"); 	//OK, accepted
		metadataMap.put("author", "author-1"); 				//will not be accepted
		//metadataMap.put("dc.contributor.author", "author-2"); 
		
		
//		// Test publication: file only -- OK
//		output += "\n" + "== PUBLICATION test: FILE only ==\n";
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, zipFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, xmlFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, txtFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, otherFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, complicatedFile);	//true
		
		
//		// Test publication: metadata only -- OK
//		output += "\n" + "== PUBLICATION test: METADATA only ==\n";
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, metadataMap);	//true
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, xmlFile); 		//true
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, txtFile); 		//false
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, otherFile); 		//false
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, complicatedFile);//false
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, zipFile); 		//false
		
		
		// Test publication: file + metadata -- OK
		output += "\n" + "== PUBLICATION test: FILE + METADATA ==\n";
		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipFile, metadataMap);			//true
		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, xmlFile, xmlFile); 				//true
		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, txtFile, xmlFile); 				//true
		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, otherFile, otherFile); 			//false
		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, complicatedFile, complicatedFile);//false
		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipFile, xmlFile); 				//true

		
		System.out.println(output);
		
	}

}
