package bwfdm.sara.project;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.db.JDBCDatabase;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.git.GitRepoWithoutProject;
import bwfdm.sara.project.RefAction.PublicationMethod;

public class Project {
	private static final String PROJECT_ATTR = Project.class.getCanonicalName();
	private static final Map<MetadataField, MetadataValue> EMPTY_METADATA;
	static {
		// create an empty dummy entry for each metadata field. this ensures
		// that all fields exist, even when they're empty.
		final Map<MetadataField, MetadataValue> map = new EnumMap<>(
				MetadataField.class);
		for (final MetadataField f : MetadataField.values())
			map.put(f, new MetadataValue(null, true));
		EMPTY_METADATA = Collections.unmodifiableMap(map);
	}

	private final GitRepo repo;
	private final String gitRepo;
	private final FrontendDatabase db;
	private final Config config;
	private String projectPath;

	private Project(final String gitRepo, final GitRepo repo,
			final Config config) {
		this.gitRepo = gitRepo;
		this.repo = repo;
		this.config = config;
		db = new JDBCDatabase(gitRepo, config);
	}

	private void checkHaveProjectPath() {
		if (projectPath == null)
			throw new NoProjectException();
	}

	/**
	 * @return a {@link GitRepo}, ie. with project path set and ready for
	 *         operations that need a project to be selected
	 */
	public GitRepo getGitRepo() {
		checkHaveProjectPath();
		return repo;
	}

	/**
	 * @return a {@link GitRepoWithoutProject}, supporting only those operations
	 *         that don't need a project selected
	 */
	public GitRepoWithoutProject getGitRepoWithoutProject() {
		return repo;
	}

	public String getRepoID() {
		return gitRepo;
	}

	public void setProjectPath(final String projectPath) {
		this.projectPath = projectPath;
		repo.setProjectPath(projectPath);
		db.setProjectPath(projectPath);
	}

	public String getProjectPath() {
		return projectPath;
	}

	public Map<MetadataField, MetadataValue> getMetadata() {
		checkHaveProjectPath();
		final Map<MetadataField, MetadataValue> metadata = new EnumMap<>(
				EMPTY_METADATA);
		db.loadMetadata(metadata);
		return Collections.unmodifiableMap(metadata);
	}

	public void setMetadata(final MetadataField field, final String value,
			final boolean auto) {
		checkHaveProjectPath();
		db.setMetadata(field, value, auto);
	}

	public Map<Ref, RefAction> getRefActions() {
		checkHaveProjectPath();
		final Map<Ref, RefAction> actions = new HashMap<>();
		db.loadRefActions(actions);
		return Collections.unmodifiableMap(actions);
	}

	public void setRefAction(final Ref ref, final PublicationMethod method,
			final String firstCommit) {
		checkHaveProjectPath();
		db.setRefAction(ref, method, firstCommit);
	}

	public static Project getInstance(final HttpSession session) {
		final Project repo = (Project) session.getAttribute(PROJECT_ATTR);
		if (repo == null)
			throw new NoSessionException();
		return repo;
	}

	/**
	 * Convenience method for calling {@link #getInstance(HttpSession)} then
	 * {@link Project#getGitRepo()}.
	 */
	public static GitRepo getGitRepo(final HttpSession session) {
		return getInstance(session).getGitRepo();
	}

	public static boolean hasInstance(final HttpSession session) {
		return session.getAttribute(PROJECT_ATTR) != null;
	}

	/**
	 * Creates a new {@link Project} instance, overwriting the previous one.
	 * Meant to be called by the {@link Authorization login / session creation
	 * code} only!
	 * 
	 * @param session
	 *            the user's {@link HttpSession}
	 * @param repoID
	 *            ID of the gitlab instance
	 * @param projectPath
	 *            path or ID of the project on gitlab (may be <code>null</code>)
	 * @param config
	 *            the global {@link Config} object (use {@link Autowired} to
	 *            have Spring inject it)
	 * @return
	 */
	public static Project createInstance(final HttpSession session,
			final String repoID, final String projectPath, final Config config) {
		final GitRepo repo = GitRepoFactory.createInstance(repoID, config);

		final Project project = new Project(repoID, repo, config);
		if (projectPath != null)
			project.setProjectPath(projectPath);
		session.setAttribute(PROJECT_ATTR, project);
		return project;
	}

	@SuppressWarnings("serial")
	public static class NoSessionException extends RuntimeException {
		private NoSessionException() {
			super("session expired or not found");
		}
	}

	@SuppressWarnings("serial")
	public static class NoProjectException extends RuntimeException {
		private NoProjectException() {
			super("no project selected");
		}
	}
}
