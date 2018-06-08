package bwfdm.sara.git.github;

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
public class GitHubRESTv3 implements GitRepo {
	/** URL for accessing the GitHub API. */
	static final String API_URL = "https://api.github.com";
	/** GitHub API version to request. */
	private static final String API_VERSION = "application/vnd.github.v3+json";
	/** Home page, used for "back to git repo". */
	private static final String HOME_URL = "https://www.github.com";
	/** OAuth2 authorization endpoint. */
	private static final String OAUTH_AUTHORIZE = "https://github.com/login/oauth/authorize";
	/** OAuth2 token service endpoint. */
	private static final String OAUTH_TOKEN = "https://github.com/login/oauth/access_token";
	/**
	 * date format pattern used by GitHub, {@link SimpleDateFormat} style.
	 * currently ISO8601 ({@code 2012-09-20T11:50:22+03:00}). Note lack of
	 * milliseconds, which are present in GitLab...
	 */
	static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";

	private final OAuthREST authRest;
	private final RESTHelper rest;
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
	public GitHubRESTv3(@JsonProperty("oauthID") final String appID,
			@JsonProperty("oauthSecret") final String appSecret) {
		authRest = new OAuthREST(API_URL, "token");
		authRest.addHeader("Accept", API_VERSION);
		rest = new RESTHelper(authRest, "");
		this.appID = appID;
		this.appSecret = appSecret;
	}

	@Override
	public GitProject getGitProject(final String project) {
		return new GitHubProject(authRest, project, token);
	}

	@Override
	public boolean hasWorkingToken() {
		if (!authRest.hasToken())
			return false;

		// test token by retrieving user info. this throws a 401 if executed
		// without a token.
		try {
			rest.getBlob(rest.uri("/user"));
			return true;
		} catch (final Exception e) {
			authRest.setToken(null);
			return false;
		}
	}

	@Override
	public UserInfo getUserInfo() {
		// FIXME Auto-generated method stub
		return new UserInfo("stefan.kombrink@uni-ulm.de",
				"stefan.kombrink@uni-ulm.de", "Stefan Kombrink");
	}

	@Override
	public RedirectView triggerAuth(final String redirURI,
			final RedirectAttributes redir, final HttpSession session) {
		if (hasWorkingToken())
			return null;

		auth = new OAuthCode(appID, appSecret, OAUTH_AUTHORIZE, OAUTH_TOKEN);
		auth.addAttribute("scope", "read:user repo");
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
		return HOME_URL;
	}

	@Override
	public List<ProjectInfo> getProjects() {
		// only list projects that the user is a member of, either directly by
		// ownership, or indirectly by group membership
		return toDataObject(rest.getList(
				rest.uri("/user/repos").queryParam("affiliation",
						"owner,organization_member"),
				new ParameterizedTypeReference<List<GHProjectInfo>>() {
				}));
	}

	static <T> List<T> toDataObject(
			final List<? extends DataObject<T>> items) {
		final ArrayList<T> list = new ArrayList<>(items.size());
		for (final DataObject<T> gldo : items)
			list.add(gldo.toDataObject());
		return list;
	}
}
