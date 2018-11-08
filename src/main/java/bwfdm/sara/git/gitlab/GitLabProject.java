package bwfdm.sara.git.gitlab;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import bwfdm.sara.auth.AuthenticatedREST;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.DataObject;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.git.Tag;
import bwfdm.sara.utils.UrlEncode;

/** high-level abstraction of the GitLab REST API. */
public class GitLabProject implements GitProject {
	private final RESTHelper rest;
	private final String guiRoot;
	private final String token;

	public GitLabProject(final AuthenticatedREST authRest, final String root,
			final String project, final String token) {
		this.token = token;
		rest = new RESTHelper(authRest, "/projects/"
				+ UrlEncode.encodePathSegment(project));
		guiRoot = root + "/" + project;
	}

	@Override
	public String getProjectViewURL() {
		return guiRoot;
	}

	@Override
	public String getEditURL(final String branch, final String path) {
		return guiRoot + "/edit/" + branch + "/" + path;
	}

	@Override
	public String getCreateURL(final String branch, final String path) {
		return guiRoot + "/new/" + branch + "/?commit_message=Add+"
				+ UrlEncode.encodeQueryParam(path) + "&file_name="
				+ UrlEncode.encodeQueryParam(path);
	}

	@Override
	public String getCloneURI() {
		// TODO should we take this from project info instead?
		// ie. is there ever any case where this doesn't work as intended?
		return guiRoot + ".git";
	}

	@Override
	public List<Branch> getBranches() {
		final ProjectInfo projectInfo = getProjectInfo();
		final List<GLBranch> list = rest.getList(
				rest.uri("/repository/branches"),
				new ParameterizedTypeReference<List<GLBranch>>() {
				});
		final ArrayList<Branch> branches = new ArrayList<>(list.size());
		for (final GLBranch glb : list)
			branches.add(glb.toBranch(projectInfo.master));
		return branches;
	}

	@Override
	public List<Tag> getTags() {
		return toDataObject(rest.getList(rest.uri("/repository/tags"),
				new ParameterizedTypeReference<List<GLTag>>() {
				}));
	}

	@Override
	public List<Commit> getCommits(final String ref, final int limit) {
		final UriComponentsBuilder req = rest.uri("/repository/commits")
				.queryParam("ref_name", ref)
				.queryParam("per_page", Integer.toString(limit));
		return toDataObject(rest.get(req,
				new ParameterizedTypeReference<List<GLCommit>>() {
				}));
	}

	@Override
	public byte[] getBlob(final String ref, final String path) {
		final UriComponentsBuilder req = rest.uri(
				"/repository/files/" + UrlEncode.encodePathSegment(path)
						+ "/raw").queryParam("ref", ref);
		try {
			return rest.getBlob(req);
		} catch (final HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND)
				return null; // file just doesn't exist
			throw e; // something serious
		}
	}

	@Override
	public void enableClone(final boolean enable) {
		// we already have access to the repo by using our OAuth token, so
		// nothing to be done here
		return;
	}

	@Override
	public void setCredentials(final TransportCommand<?, ?> tx) {
		// GitLab supports repo access using the OAuth token directly
		tx.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
				"oauth2", token));
	}

	@Override
	public ProjectInfo getProjectInfo() {
		return rest.get(rest.uri("" /* the project itself */),
				new ParameterizedTypeReference<GLProjectInfo>() {
				}).toDataObject();
	}

	private static <T> List<T> toDataObject(
			final List<? extends DataObject<T>> items) {
		return GitLabRESTv4.toDataObject(items);
	}
}
