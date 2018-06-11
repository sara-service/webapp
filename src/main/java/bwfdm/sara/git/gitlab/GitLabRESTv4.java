package bwfdm.sara.git.gitlab;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.auth.OAuthCode;
import bwfdm.sara.auth.OAuthREST;
import bwfdm.sara.git.DataObject;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.ProjectInfo;

/** high-level abstraction of the GitLab REST API. */
public class GitLabRESTv4 implements GitRepo {
	/**
	 * URL prefix for accessing the API. also defines which API version will be
	 * used.
	 */
	private static final String API_PREFIX = "/api/v4";
	/**
	 * date format pattern used by GitLab, {@link SimpleDateFormat} style.
	 * currently ISO8601 ({@code 2012-09-20T11:50:22.000+03:00}).
	 */
	static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	private final OAuthREST authRest;
	private final RESTHelper rest;
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
		if (root.endsWith("/"))
			throw new IllegalArgumentException(
					"root URL must not end with slash: " + root);

		authRest = new OAuthREST(root + API_PREFIX, "Bearer");
		rest = new RESTHelper(authRest, "");
		this.root = root;
		this.appID = appID;
		this.appSecret = appSecret;
	}

	@Override
	public GitProject getGitProject(final String project) {
		// not invalidating the old token here. it should work for any project
		// (as long as it hasn't expired yet).
		return new GitLabProject(authRest, root, project, authRest.getToken());
	}

	@Override
	public boolean hasWorkingToken() {
		if (!authRest.hasToken())
			return false;

		// looks like we do have a token. send a dummy API request to see
		// whether it's a WORKING token. downloads the user info because that
		// should always be available and doesn't depend on the project.
		try {
			getUserInfo();
			return true;
		} catch (final Exception e) {
			// doesn't look like that token is working...
			authRest.setToken(null);
			return false;
		}
	}

	@Override
	public UserInfo getUserInfo() {
		return rest.get(rest.uri("/user"),
				new ParameterizedTypeReference<GLUserInfo>() {
				}).toDataObject();
	}

	@Override
	public RedirectView triggerAuth(final String redirURI,
			final RedirectAttributes redir, final HttpSession session) {
		if (hasWorkingToken())
			return null;

		auth = new OAuthCode(appID, appSecret, root + "/oauth");
		auth.addAttribute("response_type", "code");
		return auth.trigger(redirURI, redir);
	}

	@Override
	public boolean parseAuthResponse(
			final java.util.Map<String, String> params,
			final HttpSession session) {
		if (auth == null)
			return false;

		token = auth.parse(params);
		authRest.setToken(token);
		return token != null;
	}

	@Override
	public String getHomePageURL() {
		return root;
	}

	@Override
	public List<ProjectInfo> getProjects() {
		return toDataObject(rest.getList(
				rest.uri("/projects").queryParam("simple", "true")
						.queryParam("membership", "true"),
				new ParameterizedTypeReference<List<GLProjectInfo>>() {
				}));
	}

	static <T> List<T> toDataObject(final List<? extends DataObject<T>> items) {
		final ArrayList<T> list = new ArrayList<>(items.size());
		for (final DataObject<T> gldo : items)
			list.add(gldo.toDataObject());
		return list;
	}
}
