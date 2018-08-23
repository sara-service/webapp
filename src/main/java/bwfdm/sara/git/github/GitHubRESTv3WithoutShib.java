package bwfdm.sara.git.github;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.ParameterizedTypeReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.auth.DisplayNameSplitter;
import bwfdm.sara.auth.DisplayNameSplitter.Name;
import bwfdm.sara.auth.ShibAuth;
import bwfdm.sara.git.GitRepo;

/**
 * Uses {@link GitHubRESTv3} with GitHub authentication. Not recommended because
 * it allows access to arbitrary GitHub users without further authorization.
 */
public class GitHubRESTv3WithoutShib extends GitHubRESTv3 implements GitRepo {
	private final DisplayNameSplitter nameSplitter;

	/**
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 * @param nameRegex
	 *            pattern for {@link DisplayNameSplitter}
	 */
	@JsonCreator
	public GitHubRESTv3WithoutShib(@JsonProperty("oauthID") final String appID,
			@JsonProperty("oauthSecret") final String appSecret,
			@JsonProperty("nameRegex") final String nameRegex) {
		super(appID, appSecret);
		this.nameSplitter = new DisplayNameSplitter(nameRegex);
	}

	@Override
	public UserInfo getUserInfo() {
		final GHUserInfo userInfo = rest.get(rest.uri("/user"),
				new ParameterizedTypeReference<GHUserInfo>() {
				});
		final Name name = nameSplitter
				.split(userInfo.getEffectiveDisplayName());
		return new UserInfo(userInfo.userID, getUserEmail(), name.family,
				name.given);
	}

	private String getUserEmail() {
		final List<GHEmail> emails = rest.get(rest.uri("/user/emails"),
				new ParameterizedTypeReference<List<GHEmail>>() {
				});
		for (GHEmail email : emails)
			if (email.isPrimary && email.isVerified)
				return email.address;
		throw new NoSuchElementException(
				"user has no primary, verified email!");
	}

	@Override
	public ShibAuth getShibAuth() {
		return null; // non-Shib; use ID from GitHub account
	}
}
