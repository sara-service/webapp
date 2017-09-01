package bwfdm.sara.publication;

import bwfdm.sara.publication.dspace.DSpaceConfig;
import bwfdm.sara.publication.dspace.DSpaceVersionSix;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author vk
 */
public class DemoDSpaceOrgSix extends OparuSix {

	private final DSpaceVersionSix dspaceRepo;
	private final String urlServer;

	public DemoDSpaceOrgSix(@JsonProperty("url") final String url,
			@JsonProperty("rest") final String rest,
			@JsonProperty("username") final String user,
			@JsonProperty("password") final String pass) {
		super(url, rest, user, pass);
		urlServer = url;

		dspaceRepo = new DSpaceVersionSix("DemoDSpaceOrg-6", url, rest,
				DSpaceConfig.SSL_VERIFY_DemoDSpaceOrg_SIX,
				DSpaceConfig.RESPONSE_STATUS_OK_DemoDSpaceOrg_SIX);

		System.out.println("--- DemoDSpaceOrg-6, constructor ---");
	}

	@Override
	public boolean loginPublicationRepository() {
		if (!dspaceRepo.isRestEnable()) {
			return false;
		}
		return dspaceRepo.login(user, pass);
	}

	@Override
	public String getRepositoryUrl() {
		return urlServer;
	}
}
