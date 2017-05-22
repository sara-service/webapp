package bwfdm.sara.gitlab;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

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
}
