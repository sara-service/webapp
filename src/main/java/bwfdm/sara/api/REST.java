package bwfdm.sara.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api")
public class REST {
	@GetMapping("branches")
	public List<String> getBranches(
			@RequestParam("project") final String project,
			final HttpSession session) {
		final String token = (String) session.getAttribute("gitlab_token");
		if (token == null)
			throw new IllegalStateException("not logged in");

		final UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(
						Temp.GITLAB + "/api/v4/projects/"
								+ encodePathSegment(project)
								+ "/repository/branches")
				.queryParam("access_token", token)
				// FIXME work around pagination misfeature here
				.queryParam("per_page", "100");
		final List<Branch> branches = new RestTemplate().exchange(
				builder.build(true).toUri(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Branch>>() {
				}).getBody();

		final List<String> res = new ArrayList<String>(branches.size());
		for (final Branch b : branches)
			res.add(b.name);
		return res;
	}

	@GetMapping("actions")
	public Map<String, String> getActions(final HttpSession session) {
		// FIXME load from database instead
		@SuppressWarnings("unchecked")
		final Map<String, String> actions = (Map<String, String>) session
				.getAttribute("branch_actions");
		if (actions == null)
			return Collections.emptyMap();
		return actions;
	}

	@PutMapping("actions")
	public void setActions(@RequestBody final Map<String, String> actions,
			final HttpSession session) {
		// FIXME store in database instead
		session.setAttribute("branch_actions", actions);
	}

	private static String encodePathSegment(final String project) {
		try {
			return UriUtils.encodePathSegment(project, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 unsupported?!", e);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Branch {
		@JsonProperty("name")
		private String name;
	}
}
