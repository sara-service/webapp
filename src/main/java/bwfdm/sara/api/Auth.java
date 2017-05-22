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
	private static final SecureRandom rng = new SecureRandom();

	@GetMapping("login")
	public RedirectView redirectToLogin(
			@RequestParam("project") final String project,
			final RedirectAttributes redir, final HttpSession session) {
		final byte[] random = new byte[10];
		rng.nextBytes(random);
		final String state = DatatypeConverter.printBase64Binary(random);
		session.setAttribute("oauth_state", state);
		session.setAttribute("project", project);

		redir.addAttribute("client_id", Temp.APP_ID);
		redir.addAttribute("redirect_uri", Temp.SARA + "/api/auth/redirect");
		redir.addAttribute("response_type", "code");
		redir.addAttribute("state", state);
		return new RedirectView(Temp.GITLAB + "/oauth/authorize");
	}

	@GetMapping("redirect")
	public RedirectView redirectToBranches(
			@RequestParam("code") final String code,
			@RequestParam("state") final String state,
			final RedirectAttributes redir, final HttpSession session) {
		final String correctState = (String) session
				.getAttribute("oauth_state");
		session.removeAttribute("oauth_state");
		if (correctState == null)
			throw new IllegalStateException("no oauth_state");
		if (!correctState.equals(state))
			throw new IllegalArgumentException("invalid oauth_state");

		final Map<String, String> vars = new HashMap<String, String>();
		vars.put("client_id", Temp.APP_ID);
		vars.put("client_secret", Temp.APP_SECRET);
		vars.put("code", code);
		vars.put("redirect_uri", Temp.SARA + "/api/auth/redirect");
		vars.put("grant_type", "authorization_code");
		final AccessToken auth = new RestTemplate().postForObject(Temp.GITLAB
				+ "/oauth/token", vars, AccessToken.class);
		session.setAttribute("gitlab_token", auth.token);

		redir.addAttribute("project", session.getAttribute("project"));
		return new RedirectView("/branches.html");
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
