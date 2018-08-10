package bwfdm.sara.auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Code-based OAuth authentication / authorization. */
public class OAuthCode {
	private static final SecureRandom RNG = new SecureRandom();

	private final Map<String, String> customAttributes = new HashMap<>();
	private final String appID;
	private final String appSecret;
	private final String authorizeEndpoint;
	private final String tokenEndpoint;

	private String state;
	private String redirURI;
	private String token;

	/**
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 * @param authorizeEndpoint
	 *            URL of the authorization endpoint, usually
	 *            {@code …/oauth/authorize}
	 * @param tokenEndpoint
	 *            URL of the token generation endpoint, usually
	 *            {@code …/oauth/token}
	 */
	public OAuthCode(final String appID, final String appSecret,
			final String authorizeEndpoint, final String tokenEndpoint) {
		this.appID = appID;
		this.appSecret = appSecret;
		this.authorizeEndpoint = authorizeEndpoint;
		this.tokenEndpoint = tokenEndpoint;
	}

	/**
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 * @param oauthRoot
	 *            root URL of the authorization endpoints, usually
	 *            {@code …/oauth}
	 */
	public OAuthCode(final String appID, final String appSecret,
			final String oauthRoot) {
		this(appID, appSecret, oauthRoot + "/authorize", oauthRoot + "/token");
	}

	public void addAttribute(String key, String value) {
		customAttributes.put(key, value);
	}

	/**
	 * Triggers a new authentication / authorization process, invalidation the
	 * previous one.
	 * 
	 * @param redirURI
	 *            URL that we expect the oauth server to redirect to
	 * @param redir
	 *            for passing redirect parameters
	 * 
	 * @return {@link RedirectView} redirecting to the {@code authorize}
	 *         endpoint
	 */
	public RedirectView trigger(final String redirURI,
			final RedirectAttributes redir) {
		this.redirURI = redirURI; // needed again for getting a code
		token = null;
		// some randomness to avoid CSRF
		final byte[] random = new byte[10];
		RNG.nextBytes(random);
		state = Base64.getUrlEncoder().encodeToString(random);

		// standard OAuth query
		for (String attr : customAttributes.keySet())
			redir.addAttribute(attr, customAttributes.get(attr));
		redir.addAttribute("client_id", appID);
		redir.addAttribute("redirect_uri", redirURI);
		redir.addAttribute("state", state);
		return new RedirectView(authorizeEndpoint);
	}

	/**
	 * Triggers a new authentication / authorization process, invalidation the
	 * previous one.
	 * 
	 * @param params
	 *            a {@link Map} containing all request parameters
	 * 
	 * @return the retrieved authorization token
	 */
	public String parse(final Map<String, String> params) {
		if (token != null)
			return token;

		final String responseState = params.get("state");
		if (responseState == null)
			throw new IllegalArgumentException("missing parameter 'state'");
		final String code = params.get("code");
		if (code == null)
			throw new IllegalArgumentException("missing parameter 'code'");

		// check state to avoid CSRF and replay attacks
		if (state == null)
			throw new IllegalStateException(
					"replayed or unsolicited oauth response");
		if (!responseState.equals(state))
			throw new IllegalArgumentException("forged oauth response");

		// use OAuth code to obtain a token
		final Map<String, String> vars = new HashMap<String, String>();
		vars.put("client_id", appID);
		vars.put("client_secret", appSecret);
		vars.put("code", code);
		vars.put("redirect_uri", redirURI);
		vars.put("grant_type", "authorization_code");
		final AccessToken auth = new RestTemplate().postForObject(
				tokenEndpoint, vars, AccessToken.class);
		token = auth.token;

		// this avoids replay attacks. GitLab doesn't allow codes to be reused,
		// so calling this method again wouldn't work anyway.
		state = null;
		return token;
	}

	/** data class for OAuth response. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class AccessToken {
		@JsonProperty("access_token")
		private String token;
		@JsonProperty("token_type")
		private String type;
		@JsonProperty("refresh_token")
		private String refresh;
		@JsonProperty("exires_in")
		private int expiry;
	}
}
