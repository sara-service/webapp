package bwfdm.sara.git;

import java.util.List;

public interface GitRepo {
	/** @return a list of all branches in the given project */
	public List<? extends Branch> getBranches();

	/** @return a list of all tags in the given project */
	public List<? extends Tag> getTags();

	/** @return the project metadata */
	public ProjectInfo getProjectInfo();

	/**
	 * @param ref
	 *            git ref, should be {@code heads/master} or {@code tags/test}
	 * @param limit
	 *            maximum number of items to return. GitLab clamps this to 100
	 *            max
	 * @return a list of the first few commits in a given branch or tag
	 */
	public List<? extends Commit> getCommits(final String ref, final int limit);
}
