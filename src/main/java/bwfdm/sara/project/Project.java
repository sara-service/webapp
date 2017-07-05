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

	private Project(final String gitRepo, final GitRepo repo,
			final Config config) {
		this.gitRepo = gitRepo;
		this.repo = repo;
		db = new JDBCDatabase(gitRepo, config);
	}

	public GitRepo getGitRepo() {
		return repo;
	}

	public String getRepoID() {
		return gitRepo;
	}

	public void setProjectPath(final String project) {
		repo.setProjectPath(project);
		db.setProjectPath(project);
	}

	public Map<MetadataField, MetadataValue> getMetadata() {
		final Map<MetadataField, MetadataValue> metadata = new EnumMap<>(
				EMPTY_METADATA);
		db.loadMetadata(metadata);
		return metadata;
	}

	public void setMetadata(final MetadataField field, final String value,
			final boolean auto) {
		db.setMetadata(field, value, auto);
	}

	public Map<Ref, RefAction> getRefActions() {
		final Map<Ref, RefAction> actions = new HashMap<>();
		db.loadRefActions(actions);
		return actions;
	}

	public void setRefAction(final Ref ref, final PublicationMethod method,
			final String firstCommit) {
		db.setRefAction(ref, method, firstCommit);
	}

	public static Project getInstance(final HttpSession session) {
		final Project repo = (Project) session.getAttribute(PROJECT_ATTR);
		if (repo == null)
			throw new NoSessionException();
		return repo;
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
	 *            have spring inject it)
	 * @return
	 */
	public static Project createInstance(final HttpSession session,
			final String repoID, final String projectPath, final Config config) {
		final GitRepo repo = GitRepoFactory.createInstance(session, repoID);

		final Project project = new Project(repoID, repo, config);
		if (projectPath != null)
			project.setProjectPath(projectPath);
		session.setAttribute(PROJECT_ATTR, project);
		return project;
	}

	@SuppressWarnings("serial")
	public static class NoSessionException extends RuntimeException {
	}
}
