package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionFive;

/**
 * 
 * @author vk
 */
public class OparuFive implements PublicationRepository {

	private final DSpaceVersionFive dspaceFive;
	private final String urlServer;

	// Constructor
	public OparuFive() {

		urlServer = DSpaceConfig.URL_OPARU_FIVE;

		dspaceFive = new DSpaceVersionFive("OPARU-5",
				DSpaceConfig.URL_OPARU_FIVE, DSpaceConfig.URL_OPARU_FIVE_REST,
				DSpaceConfig.SSL_VERIFY_OPARU_FIVE,
				DSpaceConfig.RESPONSE_STATUS_OK_OPARU_FIVE);

		System.out.println("--- OPARU-5, constructor ---");
	}

	@Override
	public boolean loginPublicationRepository() {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public boolean logoutPublicationRepository() {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public boolean publishElement(final String publicationLink,
			final String metadata) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public String changeElement() {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public String deleteElement() {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public String changeElementMetadata() {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public String getRepositoryUrl() {
		return urlServer;
	}

}
