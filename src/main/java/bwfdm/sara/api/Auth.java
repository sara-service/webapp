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
	private static final String STATE_ATTR = "oauth_state";
	private static final String TOKEN_ATTR = "gitlab_token";
	private static final SecureRandom rng = new SecureRandom();

	@GetMapping("login")
	public RedirectView redirectToLogin(
			@RequestParam("project") final String project,
			final RedirectAttributes redir, final HttpSession session) {
		final byte[] random = new byte[10];
		rng.nextBytes(random);
		final String state = DatatypeConverter.printBase64Binary(random);
		session.setAttribute(STATE_ATTR, state);
		session.setAttribute("project", project);

		redir.addAttribute("client_id", Config.APP_ID);
		redir.addAttribute("redirect_uri", getRedirectURI(session));
		redir.addAttribute("response_type", "code");
		redir.addAttribute("state", state);
		return new RedirectView(Config.GITLAB + "/oauth/authorize");
	}

	@GetMapping("redirect")
	public RedirectView redirectToBranches(
			@RequestParam("code") final String code,
			@RequestParam("state") final String state,
			final RedirectAttributes redir, final HttpSession session) {
		final String correctState = (String) session.getAttribute(STATE_ATTR);
		session.removeAttribute(STATE_ATTR);
		if (correctState == null)
			throw new IllegalStateException("no oauth_state");
		if (!correctState.equals(state))
			throw new IllegalArgumentException("invalid oauth_state");

		final Map<String, String> vars = new HashMap<String, String>();
		vars.put("client_id", Config.APP_ID);
		vars.put("client_secret", Config.APP_SECRET);
		vars.put("code", code);
		vars.put("redirect_uri", getRedirectURI(session));
		vars.put("grant_type", "authorization_code");
		final AccessToken auth = new RestTemplate().postForObject(Config.GITLAB
				+ "/oauth/token", vars, AccessToken.class);
		session.setAttribute(TOKEN_ATTR, auth.token);

		redir.addAttribute("project", session.getAttribute("project"));
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
