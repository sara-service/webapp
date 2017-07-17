package bwfdm.sara.git.gitlab;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import bwfdm.sara.auth.OAuthCode;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.ProjectInfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** high-level abstraction of the GitLab REST API. */
public class GitLabRESTv4 implements GitRepo {
	private final AuthenticatedREST rest = new AuthenticatedREST();
	private final String root;
	private final String appID;
	private final String appSecret;
	private OAuthCode auth;
	private String token;

	/**
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 * @param gitlab
	 *            URL to GitLab root
	 */
	@JsonCreator
	public GitLabRESTv4(@JsonProperty("url") final String root,
			@JsonProperty("oauthID") final String appID,
			@JsonProperty("oauthSecret") final String appSecret) {
		this.root = root;
		this.appID = appID;
		this.appSecret = appSecret;
	}

	@Override
	public GitProject getGitProject(final String project) {
		// not invalidating the old token here. it should work for any project
		// (as long as it hasn't expired yet).
		return new GLProject(rest, root, project, token);
	}

	@Override
	public boolean hasWorkingToken() {
		if (!rest.hasToken())
			return false;

		// looks like we do have a token. send a dummy API request to see
		// whether it's a WORKING token. downloads the user info because that
		// should always be available and doesn't depend on the project.
		try {
			rest.getBlob(UriComponentsBuilder.fromHttpUrl(root
					+ RESTHelper.API_PREFIX + "/user"));
			return true;
		} catch (final Exception e) {
			// doesn't look like that token is working...
			rest.setToken(null);
			return false;
		}
	}

	@Override
	public RedirectView triggerAuth(final String redirURI,
			final RedirectAttributes redir, final HttpSession session) {
		if (hasWorkingToken())
			return null;

		auth = new OAuthCode(appID, appSecret, root + "/oauth");
		return auth.trigger(redirURI, redir);
	}

	@Override
	public boolean parseAuthResponse(
			final java.util.Map<String, String> params,
			final HttpSession session) {
		if (auth == null)
			return false;

		token = auth.parse(params);
		rest.setToken(token);
		return token != null;
	}

	@Override
	public String getHomePageURL() {
		return root;
	}

	static <T> List<T> toDataObject(final List<? extends GLDataObject<T>> items) {
		final ArrayList<T> list = new ArrayList<>(items.size());
		for (final GLDataObject<T> gldo : items)
			list.add(gldo.toDataObject());
		return list;
	}

	@Override
	public List<ProjectInfo> getProjects() {
		final UriComponentsBuilder req = UriComponentsBuilder.fromHttpUrl(
				root + RESTHelper.API_PREFIX + "/projects").queryParam(
				"simple", "true");
		return toDataObject(RESTHelper.getList(rest, req,
				new ParameterizedTypeReference<List<GLProjectInfo>>() {
				}));
	}
}
