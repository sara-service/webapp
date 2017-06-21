package bwfdm.sara.git.gitlab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.auth.OAuthCode;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.git.RepoFile;
import bwfdm.sara.git.RepoFile.FileType;
import bwfdm.sara.git.Tag;

/** high-level abstraction of the GitLab REST API. */
public class GitLabREST extends GitRepo {
	private final String root;
	private final String appID;
	private final String appSecret;
	private RESTHelper rest;
	private OAuthCode auth;
	private String apiProject;
	private String guiProject;

	/**
	 * @param id
	 *            ID of this GitLab instance in {@code repos.properties}
	 * @param gitlab
	 *            URL to GitLab root
	 * @param appID
	 *            OAuth application ID
	 * @param appSecret
	 *            OAuth application secret
	 */
	public GitLabREST(final String id, final String root, final String appID,
			final String appSecret) {
		super(id);
		this.root = root;
		this.appID = appID;
		this.appSecret = appSecret;
	}

	public GitLabREST(final String id, final Properties args) {
		super(id);
		root = args.getProperty("root");
		appID = args.getProperty("oauth.id");
		appSecret = args.getProperty("oauth.secret");
	}

	@Override
	public void setProject(final String project) {
		apiProject = project;
		rest = new RESTHelper(root, project);
		guiProject = UrlEncode.decode(project);
	}

	@Override
	public String getProject() {
		return apiProject;
	}

	@Override
	public boolean hasWorkingToken() {
		if (!rest.hasToken())
			return false;

		try {
			getProjectInfo();
			return true;
		} catch (final IllegalArgumentException e) {
			// guess that didn't work
			return false;
		}
	}

	@Override
	public RedirectView triggerLogin(final String redirURI,
			final RedirectAttributes redir, final HttpSession session) {
		if (hasWorkingToken())
			return null;

		auth = new OAuthCode(appID, appSecret, root + "/oauth");
		return auth.trigger(redirURI, redir);
	}

	@Override
	public boolean parseLoginResponse(
			final java.util.Map<String, String> params,
			final HttpSession session) {
		if (auth == null)
			return false;

		final String token = auth.parse(params);
		rest.setToken(token);
		return token != null;
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
		final List<GLBranch> list = rest.getList("/repository/branches",
				new ParameterizedTypeReference<List<GLBranch>>() {
				});
		final ArrayList<Branch> branches = new ArrayList<>(list.size());
		for (final GLBranch branch : list) {
			final boolean isDefault = branch.name.equals(projectInfo.master);
			branches.add(new Branch(branch.name, branch.isProtected, isDefault));
		}
		return branches;
	}

	@Override
	public List<Tag> getTags() {
		final List<GLTag> list = rest.getList("/repository/tags",
				new ParameterizedTypeReference<List<GLTag>>() {
				});
		final ArrayList<Tag> tags = new ArrayList<>(list.size());
		for (final GLTag tag : list)
			// branches CAN be protected, but the GitLab API doesn't return that
			// field...
			tags.add(new Tag(tag.name, false));
		return tags;
	}

	@Override
	public List<Commit> getCommits(final String ref, final int limit) {
		final List<GLCommit> list = rest.get(
				rest.uri("/repository/commits").queryParam("ref_name", ref)
						.queryParam("per_page", Integer.toString(limit)),
				new ParameterizedTypeReference<List<GLCommit>>() {
				});
		final ArrayList<Commit> tags = new ArrayList<>(list.size());
		for (final GLCommit commit : list)
			tags.add(new Commit(commit.id, commit.title, commit.date));
		return tags;
	}

	@Override
	public byte[] getBlob(final String ref, final String path) {
		try {
			return rest.getBlob(rest.uri(
					"/repository/files/" + UrlEncode.encodePathSegment(path)
							+ "/raw").queryParam("ref", ref));
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
			rest.post(endpoint, args);
		else if (!Arrays.equals(data, existing))
			// file exists, and has actually been changed. (gitlab doesn't like
			// no-change writes because they create an empty commit.) send
			// update query.
			// note the different request method; it's otherwise identical to a
			// create query.
			rest.put(endpoint, args);
	}

	@Override
	public List<RepoFile> getFiles(final String ref, final String path) {
		final List<GLRepoFile> list = rest.getList(rest.uri("/repository/tree")
				.queryParam("path", path).queryParam("ref", ref),
				new ParameterizedTypeReference<List<GLRepoFile>>() {
				});
		final ArrayList<RepoFile> files = new ArrayList<RepoFile>(list.size());
		for (final GLRepoFile x : list)
			if (x.type.equals("tree"))
				files.add(new RepoFile(x.name, x.hash, FileType.DIRECTORY));
			else if (x.type.equals("blob"))
				files.add(new RepoFile(x.name, x.hash, FileType.FILE));
		return files;
	}

	private GLProjectInfo getGitLabProjectInfo() {
		return rest.get("" /* the project itself */,
				new ParameterizedTypeReference<GLProjectInfo>() {
				});
	}

	@Override
	public ProjectInfo getProjectInfo() {
		final GLProjectInfo info = getGitLabProjectInfo();
		return new ProjectInfo(info.name, info.description);
	}

	@Override
	public void updateProjectInfo(final String name, final String description) {
		final GLProjectInfo info = new GLProjectInfo(name, description);
		rest.put("" /* the project itself */, info);
	}
}
