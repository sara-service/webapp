package bwfdm.sara.git;

import java.io.IOException;

import org.eclipse.jgit.api.TransportCommand;

public interface ArchiveProject {
	/**
	 * Determine the URL that SARA should use to access the repository.
	 * 
	 * @return a git repository URI, in any syntax that JGit understands (but
	 *         preferably SSH-based)
	 */
	public String getPushURI();

	/** @return the persistent URL for accessing the web UI */
	public String getWebURL();

	/**
	 * @param defaultBranch
	 *            name of the "default" branch, ie. the one shown when the user
	 *            visits the project without specifying a branch
	 */
	public void setDefaultBranch(String defaultBranch);

	/**
	 * Sets the credentials for authenticating access to the repository. Can be
	 * username/password or SSH keys (or anything else supported by JGit /
	 * JSch).
	 * 
	 * @param tx
	 *            a {@link TransportCommand} that will be used to access
	 *            {@link #getPushURI()} / {@link #getDarkPushURI()} and that
	 *            should have its credentials set
	 */
	public void configureCredentials(final TransportCommand<?, ?> tx);

	/**
	 * Moves this project to permanent storage.
	 * 
	 * @throws IOException
	 *             if commit fails
	 */
	void commit() throws IOException;

	/** Deletes this project. */
	void rollback();

	/**
	 * @return <code>true</code> after {@link #commit()} was executed
	 *         successfully
	 */
	boolean isCommitted();
}
