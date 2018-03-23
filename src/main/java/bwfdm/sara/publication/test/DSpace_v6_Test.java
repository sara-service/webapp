package bwfdm.sara.publication.test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import bwfdm.sara.publication.dspace.DSpace_v6;

public class DSpace_v6_Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		//URL 
		String serviceDocumentURL = "http://134.60.51.65:8080/swordv2/servicedocument";
		String restURL = "http://134.60.51.55:8080/rest";
		
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
		System.out.println("Is repo accessible: " + dspaceRepository.isAccessible());
		
		// Check if the user is registered -- OK
		System.out.println("Is \"" + userLogin + "\" " + "registered: " + dspaceRepository.isUserRegistered(userLogin));
		
		// Check if the user is allowed to publish in the repository -- OK
		System.out.println("Is \"" + userLogin + "\" assigned:" + dspaceRepository.isUserAssigned(userLogin));
			
		// Get all available collections for the user
		userCollections = dspaceRepository.getUserAvailableCollectionTitles(userLogin);
		System.out.println("User available collections: " + userCollections);
		for(String key: userCollections.keySet()) {
			System.out.println("-- " + userCollections.get(key));
		}
		
		// Get Sara-User available collections
		saraCollections = dspaceRepository.getAvailableCollectionTitles();
		System.out.println("SARA available collections: " + saraCollections);
		for(String key: saraCollections.keySet()) {
			System.out.println("-- " + key + " : " + saraCollections.get(key));
		}
		
		// Get communities for every collection
		for(Map.Entry<String, String> collection: dspaceRepository.getAvailableCollectionTitles().entrySet()) {
			System.out.println("Communities of the collection \"" + collection.getValue() + "\"");
			communities = dspaceRepository.getCommunitiesForCollection(collection.getKey());
			for(String community: communities) {
				System.out.println("-- " + community);
			}
		}
		
		
		
		
		
		
		
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
