package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionFive;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author vk
 */
public class OparuFive implements PublicationRepositoryDeprecated {

	private final DSpaceVersionFive dspaceFive;
	private final String urlServer;
	protected final String user;
	protected final String pass;

	// Constructor
	public OparuFive(@JsonProperty("url") final String url,
			@JsonProperty("rest") final String rest,
			@JsonProperty("username") final String user,
			@JsonProperty("password") final String pass) {

		this.user = user;
		this.pass = pass;
		urlServer = url;

		dspaceFive = new DSpaceVersionFive("OPARU-5", url, rest,
				DSpaceConfig.SSL_VERIFY_OPARU_FIVE,
				DSpaceConfig.RESPONSE_STATUS_OK_OPARU_FIVE);

		System.out.println("--- OPARU-5, constructor ---");
	}

	@Override
	public boolean loginPublicationRepository() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean logoutPublicationRepository() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean publishElement(final String publicationLink,
			final String metadata) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String changeElement() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String deleteElement() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String changeElementMetadata() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getRepositoryUrl() {
		return urlServer;
	}

}
