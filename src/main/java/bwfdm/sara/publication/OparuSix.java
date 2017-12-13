package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionSix;
import bwfdm.sara.publication.dspace.dto.CommunityObjectDSpaceSix;
import bwfdm.sara.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author vk
 */
public class OparuSix implements PublicationRepositoryDeprecated {

	private final DSpaceVersionSix dspaceRepo;
	private final String urlServer;
	protected final String user;
	protected final String pass;

	// Constructor
	public OparuSix(@JsonProperty("url") final String url,
			@JsonProperty("rest") final String rest,
			@JsonProperty("username") final String user,
			@JsonProperty("password") final String pass) {

		urlServer = url;
		this.user = user;
		this.pass = pass;

		dspaceRepo = new DSpaceVersionSix("OPARU-6", url, rest,
				DSpaceConfig.SSL_VERIFY_OPARU_SIX,
				DSpaceConfig.RESPONSE_STATUS_OK_OPARU_SIX);

		System.out.println("--- OPARU-6, constructor ---");
	}

	@Override
	public boolean loginPublicationRepository() {
		if (!dspaceRepo.isRestEnable()) {
			return false;
		}
		return dspaceRepo.login(user, pass);
	}

	@Override
	public boolean logoutPublicationRepository() {
		return dspaceRepo.logout();
	}

	@Override
	public boolean publishElement(final String publicationLink,
			final String metadata) {

		final boolean isPublished = true;
		String communityID = "";
		final String collectionID = "";
		final String itemID = "";

		if (!dspaceRepo.isAuthenticated()) {
			return false;
		}

		// Get community ID / create community
		boolean communityExists = false;
		final CommunityObjectDSpaceSix[] communities = JsonUtils
				.jsonStringToObject(dspaceRepo.getAllCommunities(),
						CommunityObjectDSpaceSix[].class);
		for (final CommunityObjectDSpaceSix comm : communities) {
			if (comm.name.equals(PublicationConfig.SARA_COMMUNITY_NAME)) {
				communityExists = true;
				communityID = comm.uuid;
				break;
			}
		}
		if (!communityExists) {
			final String response = dspaceRepo.createCommunity(
					PublicationConfig.SARA_COMMUNITY_NAME, "");
			communityID = JsonUtils.jsonStringToObject(response,
					CommunityObjectDSpaceSix.class).uuid;
		}

		// Get collection ID / create collection

		// Create item

		// Update item metadata

		return true;
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
