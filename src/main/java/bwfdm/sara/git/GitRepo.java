package bwfdm.sara.git;

import java.util.List;

import bwfdm.sara.project.Project;

/**
 * This interface exposes only the methods that can be invoked on GitLab without
 * having a project selected. In most cases, {@link GitProject} is the interface
 * to use.
 */
public interface GitRepo extends AuthProvider {
	/** @return a list of all projects in GitLab */
	public List<ProjectInfo> getProjects();

	/**
	 * Get a project in this git repo by path / ID. This should attempt not to
	 * invalidate an existing authorization if possible.
	 * 
	 * <p>
	 * Intended to be called by {@link Project#setProjectPath(String)} only;
	 * call that method instead.
	 * 
	 * @param project
	 *            path / ID used to identify the project in the git repo
	 * @return the {@link GitProject}, exposing functionality that needs to have
	 *         a project path set
	 */
	public GitProject getGitProject(final String project);

	/**
	 * @return the "home page" url of the repo, used when there is no other
	 *         place to redirect the user to
	 */
	public String getHomePageURL();
}
