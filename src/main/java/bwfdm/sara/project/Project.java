package bwfdm.sara.project;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.LocalRepo;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.transfer.TransferRepo;

public class Project {
	private static final String PROJECT_ATTR = Project.class.getCanonicalName();

	private final Config config;
	private final GitRepo repo;
	private final String gitRepo;
	private final TransferRepo transferRepo;
	private FrontendDatabase db;
	private GitProject project;
	private String projectPath;

	private Project(final String gitRepo, final GitRepo repo,
			final Config config) {
		this.gitRepo = gitRepo;
		this.repo = repo;
		this.config = config;
		transferRepo = new TransferRepo(this, config);
	}

	/**
	 * @return a {@link GitRepo}, supporting only those operations that don't
	 *         need a project selected
	 */
	public GitRepo getGitRepo() {
		return repo;
	}

	public String getRepoID() {
		return gitRepo;
	}

	public synchronized void setProjectPath(final String projectPath) {
		this.projectPath = projectPath;
		project = repo.getGitProject(projectPath);
		db = new FrontendDatabase(config.newJdbcTemplate(), gitRepo,
				projectPath);
		transferRepo.invalidate();
	}

	public synchronized String getProjectPath() {
		return projectPath;
	}

	/**
	 * @return a {@link GitProject}, ie. with project path set and ready for
	 *         operations that need a project to be selected
	 */
	public synchronized GitProject getGitProject() {
		checkHaveProject();
		return project;
	}

	private void checkHaveProject() {
		if (project == null)
			throw new NoProjectException();
	}

	public FrontendDatabase getFrontendDatabase() {
		checkHaveProject();
		return db;
	}

	public TransferRepo getTransferRepo() {
		checkHaveProject();
		return transferRepo;
	}

	private void checkHaveTransferRepo() {
		if (!transferRepo.isInitialized())
			throw new NeedCloneException();
	}

	public LocalRepo getLocalRepo() {
		checkHaveTransferRepo();
		return transferRepo.getRepo();
	}

	public static Project getInstance(final HttpSession session) {
		final Project repo = (Project) session.getAttribute(PROJECT_ATTR);
		if (repo == null)
			throw new NoSessionException();
		return repo;
	}

	/**
	 * Convenience method for calling {@link #getInstance(HttpSession)} then
	 * {@link Project#getGitProject()}.
	 */
	public static GitProject getGitProject(final HttpSession session) {
		return getInstance(session).getGitProject();
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
		final GitRepo repo = config.getGitRepoFactory(repoID).newGitRepo();

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

	@SuppressWarnings("serial")
	public static class NeedCloneException extends RuntimeException {
		private NeedCloneException() {
			super("need to clone the repository first");
		}
	}
}
