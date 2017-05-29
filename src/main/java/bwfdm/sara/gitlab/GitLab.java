package bwfdm.sara.gitlab;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

/** high-level abstraction of the GitLab API. */
public class GitLab {
	private final GitLabREST rest;

	public GitLab(final String gitlab, final String project, final String token) {
		rest = new GitLabREST(gitlab, project, token);
	}

	public List<Branch> getBranches() {
		return rest.getList("/repository/branches",
				new ParameterizedTypeReference<List<Branch>>() {
				});
	}

	public List<Tag> getTags() {
		return rest.getList("/repository/tags",
				new ParameterizedTypeReference<List<Tag>>() {
				});
	}

	public ProjectInfo getProjectInfo() {
		return rest.get("" /* the project itself */,
				new ParameterizedTypeReference<ProjectInfo>() {
				});
	}

	public List<Commit> getCommits(final String ref, final int limit) {
		return rest.get(
				rest.uri("/repository/commits").queryParam("ref_name", ref)
						.queryParam("per_page", Integer.toString(limit)),
				new ParameterizedTypeReference<List<Commit>>() {
				});
	}
}
