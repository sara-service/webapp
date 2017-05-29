package bwfdm.sara.api;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/auth")
public class Auth {
	private static final String PROJECT_ATTR = "project";
	private static final String STATE_ATTR = "oauth_state";
	private static final String TOKEN_ATTR = "gitlab_token";
	private static final SecureRandom rng = new SecureRandom();

	@GetMapping("login")
	public RedirectView triggerOAuth(
			@RequestParam(PROJECT_ATTR) final String project,
			final RedirectAttributes redir, final HttpSession session) {
		// if we already have a token, don't trigger authentication again
		if (session.getAttribute(TOKEN_ATTR) != null)
			return redirectToBranches(redir, session);

		// some randomness to avoid CSRF
		final byte[] random = new byte[10];
		rng.nextBytes(random);
		final String state = DatatypeConverter.printBase64Binary(random);
		session.setAttribute(STATE_ATTR, state);
		// remember project in session to preserve it across login. not
		// otherwise used so the user can have more than one active tab.
		session.setAttribute(PROJECT_ATTR, project);

		// standard OAuth query
		redir.addAttribute("client_id", Config.APP_ID);
		redir.addAttribute("redirect_uri", getRedirectURI(session));
		redir.addAttribute("response_type", "code");
		redir.addAttribute("state", state);
		return new RedirectView(Config.GITLAB + "/oauth/authorize");
	}

	@GetMapping("redirect")
	public RedirectView getOAuthToken(@RequestParam("code") final String code,
			@RequestParam("state") final String state,
			final RedirectAttributes redir, final HttpSession session) {
		// if we already have a token, we're done here. could happen if the user
		// somehow reloads this URL.
		if (session.getAttribute(TOKEN_ATTR) != null)
			return redirectToBranches(redir, session);

		// check state to avoid CSRF and replay attacks
		final String correctState = (String) session.getAttribute(STATE_ATTR);
		session.removeAttribute(STATE_ATTR);
		if (correctState == null)
			throw new IllegalStateException("no oauth_state");
		if (!correctState.equals(state))
			throw new IllegalArgumentException("invalid oauth_state");

		// use OAuth code to obtain a token from GitLab
		final Map<String, String> vars = new HashMap<String, String>();
		vars.put("client_id", Config.APP_ID);
		vars.put("client_secret", Config.APP_SECRET);
		vars.put("code", code);
		vars.put("redirect_uri", getRedirectURI(session));
		vars.put("grant_type", "authorization_code");
		final AccessToken auth = new RestTemplate().postForObject(Config.GITLAB
				+ "/oauth/token", vars, AccessToken.class);
		session.setAttribute(TOKEN_ATTR, auth.token);

		return redirectToBranches(redir, session);
	}

	private RedirectView redirectToBranches(final RedirectAttributes redir,
			final HttpSession session) {
		redir.addAttribute(PROJECT_ATTR, session.getAttribute(PROJECT_ATTR));
		return new RedirectView("/branches.html");
	}

	private String getRedirectURI(final HttpSession session) {
		return Config.getWebRoot(session) + "/api/auth/redirect";
	}

	public static String getToken(final HttpSession session) {
		final Object attr = session.getAttribute(TOKEN_ATTR);
		if (attr == null)
			throw new IllegalStateException("not GitLab token available");
		return (String) attr;
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
