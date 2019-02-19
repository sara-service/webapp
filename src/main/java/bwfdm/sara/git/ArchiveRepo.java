package bwfdm.sara.git;

import org.eclipse.jgit.lib.PersonIdent;

import bwfdm.sara.project.ArchiveMetadata;

public interface ArchiveRepo {
	/** Committer name used when none is configured in the database. */
	public final String DEFAULT_COMMITTER_NAME = "SARA Service";

	/**
	 * Creates a project on the archival GitLab.
	 * 
	 * @param id
	 *            unique name for the new project
	 * @param visible
	 *            if <code>true</code>, the project must be publicly accessible.
	 *            if <code>false</code>, it must be invisible
	 * @param meta
	 *            user's provided metadata, used to fill project metadata if
	 *            necessary (may be ignored in part or entirely)
	 * @return an {@link ArchiveProject} containing the properties of the
	 *         project
	 * @throws ProjectExistsException
	 *             if the named project already exists
	 */
	public ArchiveProject createProject(String id, boolean visible,
			ArchiveMetadata meta) throws ProjectExistsException;

	/**
	 * Determines the committer identity to be used when SARA commits it
	 * metadata to the archive repo.
	 * 
	 * @return committer identity as a {@link PersonIdent}
	 */
	public PersonIdent getMetadataCommitter();

	@SuppressWarnings("serial")
	public static class ProjectExistsException extends Exception {
		public ProjectExistsException(final String name) {
			super("project " + name + " already exists");
		}
	}
}
