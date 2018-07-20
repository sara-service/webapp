package bwfdm.sara.git.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import bwfdm.sara.auth.AuthenticatedREST;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.DataObject;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.git.Tag;
import bwfdm.sara.project.Ref;

/** high-level abstraction of the GitLab REST API. */
public class GitHubProject implements GitProject {
	private final RESTHelper rest;
	private final String token;
	private GHProjectInfo info;

	public GitHubProject(final AuthenticatedREST authRest, 
			final String project, final String token) {
		this.token = token;
		// FIXME how to handle organization-under-username here?
		// 5ar4/test goes to https://api.github.com/repos/5ar4/test
		// note unescaped slash, unlike gitlab!
		rest = new RESTHelper(authRest, "/repos/" + project);

		info = rest.get(rest.uri("" /* this project itself */),
				new ParameterizedTypeReference<GHProjectInfo>() {
				});
	}

	@Override
	public String getProjectViewURL() {
		return info.webURL;
	}

	@Deprecated
	@Override
	public String getEditURL(final String branch, final String path) {
		// FIXME check whether to encode paths with slashes
		// (almost certainly not)
		// note: this is undocumented!
		return getProjectViewURL() + "/edit/" + branch + "/" + path;
	}

	@Deprecated
	@Override
	public String getCreateURL(final String branch, final String path) {
		// FIXME allow this to be disabled, or find a way to implement it
		// the "new file" url is: getProjectViewURL() + "/new/" + branch
		// but you cannot actually specify a filename or commit message!
		// for now, minimal implementation:
		return getProjectViewURL() + "/new/" + branch;
	}

	@Override
	public String getCloneURI() {
		return info.cloneURL;
	}

	@Override
	public List<Branch> getBranches() {
		final List<GHBranch> list = rest.getList(rest.uri("/branches"),
				new ParameterizedTypeReference<List<GHBranch>>() {
				});
		final ArrayList<Branch> branches = new ArrayList<>(list.size());
		for (final GHBranch glb : list)
			branches.add(glb.toBranch(info.master));
		return branches;
	}

	@Override
	public List<Tag> getTags() {
		return toDataObject(rest.getList(rest.uri("/tags"),
				new ParameterizedTypeReference<List<GHTag>>() {
				}));
	}

	@Override
	public List<Commit> getCommits(final String ref, final int limit) {
		// FIXME this is ambiguous when tag and branch names overlap!
		// how does GitHub handle that? do they forbid it? does it simply pick
		// one at random??
		final UriComponentsBuilder req = rest.uri("/commits")
				.queryParam("sha", new Ref(ref).name)
				.queryParam("per_page", Integer.toString(limit));
		return toDataObject(rest.get(req,
				new ParameterizedTypeReference<List<GHCommit>>() {
				}));
	}

	@Override
	public byte[] getBlob(final String ref, final String path) {
		// FIXME download file without base64 deocoding?
		// - could use raw.githubusercontent.com (download_url), but does that
		// work for private projects?
		// - could use git blob api (git_url), if that returns the right data
		// format
		// - could use
		final GHFile file = getFileInfo(ref, path);
		if (!file.type.equals("file"))
			return null;
		if (!file.encoding.equals("base64"))
			throw new RuntimeException("unsupported encoding: file " + path
					+ " is encoded with " + file.encoding);
		return Base64Utils.decodeFromString(file.data);
	}

	private GHFile getFileInfo(final String ref, final String path) {
		final UriComponentsBuilder req = rest.uri("/contents/" + path)
				.queryParam("ref", ref);
		try {
			return rest.get(req,
					new ParameterizedTypeReference<GHFile>() {
					});
		} catch (final HttpClientErrorException e) {
			// FIXME check whether that's what it actually returns!
			if (e.getStatusCode() == HttpStatus.NOT_FOUND)
				return null; // file just doesn't exist
			throw e; // something serious
		}
	}

	@Override
	public void putBlob(final String branch, final String path,
			final String commitMessage, final byte[] data) {
		final HashMap<String, String> args = new HashMap<>();
		args.put("branch", branch);
		args.put("message", commitMessage);
		args.put("content", Base64Utils.encodeToString(data));

		final UriComponentsBuilder endpoint = rest.uri("/contents/" + path);
		GHFile existing = getFileInfo("heads/" + branch, path);
		if (existing != null)
			args.put("sha", existing.sha1);
		// FIXME what does GitHub do for non-changes?
		// GitLab needs:
		// if (!Arrays.equals(data, existing.data))
		rest.put(endpoint, args);
	}

	@Override
	public void enableClone(final boolean enable) {
		// we already have access to the repo by using our OAuth token, so
		// nothing to be done here
		return;
	}

	@Override
	public void setCredentials(final TransportCommand<?, ?> tx) {
		// FIXME test!
		tx.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token,
				"x-oauth-basic"));
	}

	@Override
	public ProjectInfo getProjectInfo() {
		return info.toDataObject();
	}

	@Override
	public void updateProjectInfo(final String name, final String description) {
		// TODO implement this, at least partially
		// (project name is impractical to change)
		// final GHProjectInfo info = new GHProjectInfo(name, description);
		// rest.put(rest.uri("" /* the project itself */), info);
	}

	private static <T> List<T> toDataObject(
			final List<? extends DataObject<T>> items) {
		return GitHubRESTv3.toDataObject(items);
	}
}
