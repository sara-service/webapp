package bwfdm.sara.git.gitlab;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import bwfdm.sara.git.Branch;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.Tag;

/** high-level abstraction of the GitLab API. */
public class GitLab implements GitRepo {
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

	@Override
	public List<? extends Branch> getBranches() {
		final GLProjectInfo projectInfo = getProjectInfo();
		final List<? extends GLBranch> list = rest.getList(
				"/repository/branches",
				new ParameterizedTypeReference<List<GLBranch>>() {
				});
		for (final GLBranch branch : list)
			if (branch.name.equals(projectInfo.master))
				branch.isDefault = true;
		return list;
	}

	@Override
	public List<? extends Tag> getTags() {
		return rest.getList("/repository/tags",
				new ParameterizedTypeReference<List<GLTag>>() {
				});
	}

	@Override
	public GLProjectInfo getProjectInfo() {
		return rest.get("" /* the project itself */,
				new ParameterizedTypeReference<GLProjectInfo>() {
				});
	}

	@Override
	public List<GLCommit> getCommits(final String ref, final int limit) {
		return rest.get(
				rest.uri("/repository/commits").queryParam("ref_name", ref)
						.queryParam("per_page", Integer.toString(limit)),
				new ParameterizedTypeReference<List<GLCommit>>() {
				});
	}
}
