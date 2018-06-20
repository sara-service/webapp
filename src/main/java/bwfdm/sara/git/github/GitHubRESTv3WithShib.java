package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.auth.ShibAuth;

/** Extends {@link GitHubRESTv3} to add Shibboleth authentication. */
// TODO this can basically wrap every GitRepo; maybe reimplement it so it does?
public class GitHubRESTv3WithShib extends GitHubRESTv3 {
	private final ShibAuth shib;

	/**
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 * @param displayNameAttribute
	 *            attribute containing display name from Shibboleth (usually
	 *            {@code display-name})
	 * @param displayEmail
	 *            attribute containing email from Shibboleth (usually
	 *            {@code email})
	 * @param userIDAttribute
	 *            attribute containing persistent user ID from Shibboleth
	 *            (usually {@code persistent-id} to use the eduPersonTargetedID)
	 */
	@JsonCreator
	public GitHubRESTv3WithShib(@JsonProperty("oauthID") final String appID,
			@JsonProperty("oauthSecret") final String appSecret,
			@JsonProperty("shibName") final String displayNameAttribute,
			@JsonProperty("shibEmail") final String emailAttribute,
			@JsonProperty("shibID") final String userIDAttribute) {
		super(appID, appSecret);
		shib = new ShibAuth(displayNameAttribute, emailAttribute,
				userIDAttribute);
	}

	@Override
	public boolean hasWorkingToken() {
		return shib.hasValidInfo() && super.hasWorkingToken();
	}

	@Override
	public ShibAuth getShibAuth() {
		return shib;
	}

	@Override
	public UserInfo getUserInfo() {
		return shib.getUserInfo();
	}
}
