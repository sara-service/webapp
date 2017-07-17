package bwfdm.sara.git.gitlab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.Contributor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.git.RepoFile;
import bwfdm.sara.git.Tag;

/** high-level abstraction of the GitLab REST API. */
public class GLProject implements GitProject {
	private final String root;
	private final RESTHelper helper;
	private final String guiProject;
	private final String token;

	public GLProject(final AuthenticatedREST rest, final String root,
			final String project, final String token) {
		this.root = root;
		this.token = token;
		helper = new RESTHelper(rest, root, project);
		guiProject = UrlEncode.decode(project);
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

	@Override
	public List<Tag> getTags() {
		return GitLabRESTv4.toDataObject(helper.getList("/repository/tags",
				new ParameterizedTypeReference<List<GLTag>>() {
				}));
	}

	@Override
	public List<Commit> getCommits(final String ref, final int limit) {
		final UriComponentsBuilder req = helper.uri("/repository/commits")
				.queryParam("ref_name", ref)
				.queryParam("per_page", Integer.toString(limit));
		return GitLabRESTv4.toDataObject(helper.get(req,
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
		return GitLabRESTv4.toDataObject(helper.getList(
				helper.uri("/repository/tree").queryParam("path", path)
						.queryParam("ref", ref),
				new ParameterizedTypeReference<List<GLRepoFile>>() {
				}));
	}

	@Override
	public List<Contributor> getContributors(final String ref) {
		// FIXME this definitely shouldn't just ignore the ref!
		// GitLab doesn't take a ref= parameter to the API...
		// TODO just do this on the local clone later on
		return GitLabRESTv4.toDataObject(helper.getList(
				helper.uri("/repository/contributors"),
				new ParameterizedTypeReference<List<GLContributor>>() {
				}));
	}

	@Override
	public void enableClone(final boolean enable) {
		// we already have access to the repo by using our OAuth token, so
		// nothing to be done here
		return;
	}

	@Override
	public String getCloneURI() {
		return root + "/" + guiProject + ".git";
	}

	@Override
	public CredentialsProvider getCloneCredentials() {
		// GitLab supports repo access using the OAuth token directly
		return new UsernamePasswordCredentialsProvider("oauth2", token);
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
}
