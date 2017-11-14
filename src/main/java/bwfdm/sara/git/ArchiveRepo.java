package bwfdm.sara.git;

import java.util.Map;
import java.util.NoSuchElementException;

import bwfdm.sara.project.MetadataField;

public interface ArchiveRepo {
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
			Map<MetadataField, String> meta) throws ProjectExistsException;

	/**
	 * Get a handle to an existing project on the archival GitLab.
	 * 
	 * @param id
	 *            unique name for the new project
	 * @return an {@link ArchiveProject} containing the properties of the
	 *         project
	 * @throws NoSuchElementException
	 *             if there named project doesn't exist
	 */
	public ArchiveProject getProject(String id) throws NoSuchElementException;

	@SuppressWarnings("serial")
	public static class ProjectExistsException extends Exception {
		public ProjectExistsException(final String name) {
			super("project " + name + " already exists");
		}
	}
}
