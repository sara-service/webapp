package bwfdm.sara;

import javax.servlet.http.HttpSession;

import bwfdm.sara.api.Authorization;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;

public class ProjectFactory {
	private static final String PROJECT_ATTR = Project.class.getCanonicalName();

	public static Project getInstance(final HttpSession session) {
		final Project repo = (Project) session.getAttribute(PROJECT_ATTR);
		if (repo == null)
			throw new NoSessionException();
		return repo;
	}

	@SuppressWarnings("serial")
	public static class NoSessionException extends RuntimeException {
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
		session.setAttribute(PROJECT_ATTR, project);
		return project;
	}
}
