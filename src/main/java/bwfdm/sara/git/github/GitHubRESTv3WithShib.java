package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.auth.DisplayNameSplitter;
import bwfdm.sara.auth.ShibAuth;

/** Uses {@link GitHubRESTv3} with Shibboleth authentication. */
public class GitHubRESTv3WithShib extends GitHubRESTv3 {
	private final ShibAuth shib;

	/**
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 * @param surnameAttribute
	 *            attribute containing surname from Shibboleth (usually
	 *            {@code sn})
	 * @param givenNameAttribute
	 *            attribute containing given name from Shibboleth (usually
	 *            {@code given-name})
	 * @param emailAttribute
	 *            attribute containing email from Shibboleth (usually
	 *            {@code email})
	 * @param userIDAttribute
	 *            attribute containing persistent user ID from Shibboleth
	 *            (usually {@code persistent-id} to use the eduPersonTargetedID)
	 * @param displayNameAttribute
	 *            attribute containing display name from Shibboleth (usually
	 *            {@code display-name}). may be <code>null</code> to ignore the
	 *            display name.
	 * @param nameRegex
	 *            pattern for {@link DisplayNameSplitter}. <code>null</code>
	 *            when not using the display name.
	 */
	@JsonCreator
	public GitHubRESTv3WithShib(@JsonProperty("oauthID") final String appID,
			@JsonProperty("oauthSecret") final String appSecret,
			@JsonProperty("shibSurname") final String surnameAttribute,
			@JsonProperty("shibGivenName") final String givenNameAttribute,
			@JsonProperty("shibEmail") final String emailAttribute,
			@JsonProperty("shibID") final String userIDAttribute,
			@JsonProperty(value = "shibDisplayName", required = false) final String displayNameAttribute,
			@JsonProperty(value = "nameRegex", required = false) final String nameRegex) {
		super(appID, appSecret);
		shib = new ShibAuth(surnameAttribute, givenNameAttribute,
				emailAttribute, userIDAttribute, displayNameAttribute,
				nameRegex);
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
