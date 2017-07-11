package bwfdm.sara.git.gitlab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import bwfdm.sara.auth.OAuthCode;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.Contributor;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.git.RepoFile;
import bwfdm.sara.git.Tag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** high-level abstraction of the GitLab REST API. */
public class GitLabRESTv4 implements GitRepo {
	private final AuthenticatedREST rest = new AuthenticatedREST();
	private final String root;
	private final String appID;
	private final String appSecret;
	private RESTHelper helper;
	private OAuthCode auth;
	private String guiProject;
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
	public void setProjectPath(final String project) {
		guiProject = UrlEncode.decode(project);
		// not invalidating the old token here. it should work for any project
		// (as long as it hasn't expired yet).
		helper = new RESTHelper(rest, root, project);
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

	@Override
	public String getProjectViewURL() {
		return root + "/" + guiProject;
	}

	@Override
	public String getEditURL(final String branch, final String path) {
		return root + "/" + guiProject + "/edit/" + branch + "/" + path;
	}

	@Override
	public String getCreateURL(final String branch, final String path) {
		return root + "/" + guiProject + "/new/" + branch
				+ "/?commit_message=Add+" + UrlEncode.encodeQueryParam(path)
				+ "&file_name=" + UrlEncode.encodeQueryParam(path);
	}

	@Override
	public List<Branch> getBranches() {
		final GLProjectInfo projectInfo = getGitLabProjectInfo();
		final List<GLBranch> list = helper.getList("/repository/branches",
				new ParameterizedTypeReference<List<GLBranch>>() {
				});
		final ArrayList<Branch> branches = new ArrayList<>(list.size());
		for (final GLBranch glb : list)
			branches.add(glb.toBranch(projectInfo.master));
		return branches;
	}

	private <T> List<T> toDataObject(final List<? extends GLDataObject<T>> items) {
		final ArrayList<T> list = new ArrayList<>(items.size());
		for (final GLDataObject<T> gldo : items)
			list.add(gldo.toDataObject());
		return list;
	}

	@Override
	public List<Tag> getTags() {
		return toDataObject(helper.getList("/repository/tags",
				new ParameterizedTypeReference<List<GLTag>>() {
				}));
	}

	@Override
	public List<Commit> getCommits(final String ref, final int limit) {
		final UriComponentsBuilder req = helper.uri("/repository/commits")
				.queryParam("ref_name", ref)
				.queryParam("per_page", Integer.toString(limit));
		return toDataObject(helper.get(req,
				new ParameterizedTypeReference<List<GLCommit>>() {
				}));
	}

	@Override
	public byte[] getBlob(final String ref, final String path) {
		final UriComponentsBuilder req = helper.uri(
				"/repository/files/" + UrlEncode.encodePathSegment(path)
						+ "/raw").queryParam("ref", ref);
		try {
			return helper.getBlob(req);
		} catch (final HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND)
				return null; // file just doesn't exist
			throw e; // something serious
		}
	}

	@Override
	public void putBlob(final String branch, final String path,
			final String commitMessage, final byte[] data) {
		final String endpoint = "/repository/files/"
				+ UrlEncode.encodePathSegment(path);
		final HashMap<String, String> args = new HashMap<>();
		args.put("branch", branch);
		args.put("commit_message", commitMessage);
		args.put("encoding", "base64");
		args.put("content", Base64Utils.encodeToString(data));

		final byte[] existing = getBlob("heads/" + branch, path);
		if (existing == null)
			// file doesn't exist; send create query.
			helper.post(endpoint, args);
		else if (!Arrays.equals(data, existing))
			// file exists, and has actually been changed. (gitlab doesn't like
			// no-change writes because they create an empty commit.) send
			// update query.
			// note the different request method; it's otherwise identical to a
			// create query.
			helper.put(endpoint, args);
	}

	@Override
	public List<RepoFile> getFiles(final String ref, final String path) {
		return toDataObject(helper.getList(helper.uri("/repository/tree")
				.queryParam("path", path).queryParam("ref", ref),
				new ParameterizedTypeReference<List<GLRepoFile>>() {
				}));
	}

	@Override
	public List<Contributor> getContributors(final String ref) {
		// FIXME this definitely shouldn't just ignore the ref!
		// TODO tell GitLAb to add a ref= parameter to the API...
		return toDataObject(helper.getList(
				helper.uri("/repository/contributors"),
				new ParameterizedTypeReference<List<GLContributor>>() {
				}));
	}

	private GLProjectInfo getGitLabProjectInfo() {
		return helper.get("" /* the project itself */,
				new ParameterizedTypeReference<GLProjectInfo>() {
				});
	}

	@Override
	public ProjectInfo getProjectInfo() {
		return getGitLabProjectInfo().toDataObject();
	}

	@Override
	public void updateProjectInfo(final String name, final String description) {
		final GLProjectInfo info = new GLProjectInfo(name, description);
		helper.put("" /* the project itself */, info);
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
