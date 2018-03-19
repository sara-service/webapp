package bwfdm.sara.publication.test;


import bwfdm.sara.publication.dspace.DSpace_v6;

public class DSpace_v6_Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println("DSpace_v6 testing.");
		
		// URL to service document, DSpace-6.3 
		String serviceDocumentURL = "http://134.60.51.65:8080/swordv2/servicedocument";
		
		// SARA credentials
		String saraUser = "project-sara@uni-konstanz.de";
		String saraPassword = "SaraTest";
		
		// Test user
		String userLogin = "volodymyr.kushnarenko@uni-ulm.de"; 
				
		
		// Create DSpace_v6 repository
		DSpace_v6 dspaceRepository = new DSpace_v6(serviceDocumentURL, saraUser, saraPassword);
		
		// Check if repository is accessible -- OK
		System.out.println("Is repo accessible: " + dspaceRepository.isAccessible());
		
		// Check if the user is registered -- OK
		System.out.println("Is \"" + userLogin + "\" " + "registered: " + dspaceRepository.isUserRegistered(userLogin));
		
		// Check if the user is allowed to publish in the repository -- OK
		System.out.println("Is \"" + userLogin + "\" assigned:" + dspaceRepository.isUserAssigned(userLogin));
			

	}

}
