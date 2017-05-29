package bwfdm.sara.gitlab;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

/** high-level abstraction of the GitLab API. */
public class GitLab {
	private final GitLabREST rest;

	/**
	 * @param gitlab
	 *            URL to GitLab root
	 * @param project
	 *            name of project whose API to access
	 * @param token
	 *            GitLab OAuth token
	 */
	public GitLab(final String gitlab, final String project, final String token) {
		rest = new GitLabREST(gitlab, project, token);
	}

	/** @return a list of all branches in the given project */
	public List<Branch> getBranches() {
		return rest.getList("/repository/branches",
				new ParameterizedTypeReference<List<Branch>>() {
				});
	}

	/** @return a list of all tags in the given project */
	public List<Tag> getTags() {
		return rest.getList("/repository/tags",
				new ParameterizedTypeReference<List<Tag>>() {
				});
	}

	/** @return the project metadata */
	public ProjectInfo getProjectInfo() {
		return rest.get("" /* the project itself */,
				new ParameterizedTypeReference<ProjectInfo>() {
				});
	}

	/**
	 * @param ref
	 *            git ref, should be {@code heads/master} or {@code tags/test}
	 * @param limit
	 *            maximum number of items to return. GitLab clamps this to 100
	 *            max
	 * @return a list of the first few commits in a given branch or tag
	 */
	public List<Commit> getCommits(final String ref, final int limit) {
		return rest.get(
				rest.uri("/repository/commits").queryParam("ref_name", ref)
						.queryParam("per_page", Integer.toString(limit)),
				new ParameterizedTypeReference<List<Commit>>() {
				});
	}
}
