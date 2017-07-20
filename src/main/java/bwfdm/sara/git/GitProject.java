package bwfdm.sara.git;

import java.util.List;

import org.eclipse.jgit.transport.CredentialsProvider;

/**
 * This interface exposes all GitLab methods that need to have a project set via
 * {@link #getGitProject(String)}.
 */
public interface GitProject {
	/** @return the url of the "main" page to view a project */
	public String getProjectViewURL();

	/**
	 * @param branch
	 *            the name of the branch containing the file to edit
	 * @param path
	 *            full, absolute path to file in repo
	 * @return the url of a page where the user can edit the file
	 */
	public String getEditURL(final String branch, String path);

	/**
	 * @param branch
	 *            the name of the branch in which the file is to be created
	 * @param path
	 *            full, absolute path to file in repo
	 * @return the url of a page where the user can create such a file
	 */
	public String getCreateURL(String branch, String path);

	/** @return a list of all branches in the given project */
	public List<Branch> getBranches();

	/** @return a list of all tags in the given project */
	public List<Tag> getTags();

	/** @return the project metadata */
	public ProjectInfo getProjectInfo();

	/**
	 * Updates the project metadata with the fields provided. <code>null</code>
	 * means to keep the current value.
	 * 
	 * @param name
	 *            new project name, or <code>null</code>
	 * @param description
	 *            new project description, or <code>null</code>
	 */
	public void updateProjectInfo(String name, String description);

	/**
	 * Enables or disables SARA access to the repo. Can be used to add a system
	 * user to the project. If there is a system-wide user with access to the
	 * repo, this method can be empty.
	 * 
	 * @param enable
	 *            <code>true</code> to enable acces for SARA user,
	 *            <code>false</code> to disable
	 */
	public void enableClone(boolean enable);

	/**
	 * Determine the URL that SARA should use to access the repository. This can
	 * be the same URL that the user would use, but can be a "special" URL as
	 * well, for example to bypass authentication.
	 * 
	 * @return a git repository URI, in any syntax that JGit understands (but
	 *         preferably SSH-based)
	 */
	public String getCloneURI();

	/**
	 * Obtains the credentials for authenticating access to the repository. Can
	 * be username/password or SSH keys (or anything else supported by JGit /
	 * JSch).
	 * <p>
	 * Must be bracketed in calls to {@link #enableClone(boolean)} because the
	 * credentials might only be created by {@code enableClone(true)}.
	 * 
	 * @return a JGit {@link CredentialsProvider} to use for authentication when
	 *         accessing {@link #getCloneURI()}
	 */
	public CredentialsProvider getCloneCredentials();

	/**
	 * @param ref
	 *            git ref, should be {@code heads/master} or {@code tags/test}
	 * @param limit
	 *            maximum number of items to return. GitLab clamps this to 100
	 *            max
	 * @return a list of the first few commits in a given branch or tag
	 */
	public List<Commit> getCommits(final String ref, final int limit);

	/**
	 * @param ref
	 *            branch or tag containing the file, in git ref format (ie.
	 *            {@code heads/master} or {@code tags/test})
	 * @param path
	 *            full path to a file in the repo, without the initial slash
	 * @return the contents of the file as a byte array, or <code>null</code> if
	 *         the file doesn't exist
	 */
	public byte[] getBlob(String ref, String path);

	/**
	 * Commits a file to the repo, either updating or creating it. This
	 * obviously only works for branches.
	 * 
	 * @param branch
	 *            name of the branch to modify (ie. {@code master})
	 * @param path
	 *            full path to a file in the repo, without the initial slash
	 * @param commitMessage
	 *            message for the commit that creates or modifies the file
	 * @param data
	 *            the contents of the file as a byte array (non-
	 *            <code>null</code>)
	 */
	public void putBlob(String branch, String path, String commitMessage,
			byte[] data);

	/**
	 * @param ref
	 *            branch or tag containing the directory to list, in git ref
	 *            format (ie. {@code heads/master} or {@code tags/test})
	 * @param path
	 *            full path to a directory in the repo, without the initial
	 *            slash (ie. the root directory is {@code ""})
	 * @return a list of files in that directory
	 */
	public List<RepoFile> getFiles(String ref, String path);

	/**
	 * @param ref
	 *            branch, tag or commit to analyze, in git ref format (ie.
	 *            {@code heads/master} or {@code tags/test})
	 * @return a list of contributors appearing in that branch / tag
	 */
	public List<Contributor> getContributors(String ref);
}
