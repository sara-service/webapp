package bwfdm.sara.project;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import bwfdm.sara.api.Authorization;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;

public class Project {
	private static final String PROJECT_ATTR = Project.class.getCanonicalName();

	private final GitRepo repo;
	private final String gitRepo;
	private final BasicMetaData metadata;
	private final Map<String, RefAction> actions = new HashMap<String, RefAction>();

	private Project(final String gitRepo, final GitRepo repo) {
		this.gitRepo = gitRepo;
		this.repo = repo;
		metadata = new BasicMetaData();
	}

	public GitRepo getGitRepo() {
		return repo;
	}

	public String getRepoID() {
		return gitRepo;
	}

	public BasicMetaData getMetadata() {
		return metadata;
	}

	public Map<String, RefAction> getRefActions() {
		return actions;
	}

	public void loadFromDatabase() {
		// TODO
	}

	public void saveToDatabase() {
		// TODO
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
	 * @return
	 */
	public static Project createInstance(final HttpSession session,
			final String repoID, final String projectPath) {
		final GitRepo repo = GitRepoFactory.createInstance(session, repoID);
		if (projectPath != null)
			repo.setProjectPath(projectPath);

		final Project project = new Project(repoID, repo);
		project.loadFromDatabase();
		session.setAttribute(PROJECT_ATTR, project);
		return project;
	}

	@SuppressWarnings("serial")
	public static class NoSessionException extends RuntimeException {
	}
}
