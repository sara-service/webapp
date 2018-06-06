package bwfdm.sara.project;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.transfer.CloneTask;
import bwfdm.sara.transfer.PushTask;
import bwfdm.sara.transfer.Task.TaskStatus;
import bwfdm.sara.transfer.TransferRepo;

// FIXME rename to ArchiveSession for consistency with PublicationSession
public class Project {
	private static final String PROJECT_ATTR = Project.class.getCanonicalName();

	private final Config config;
	private final GitRepo repo;
	private final String gitRepo;
	private TransferRepo transferRepo;
	private MetadataExtractor metadataExtractor;
	private FrontendDatabase db;
	private GitProject project;
	private String projectPath;
	private CloneTask clone;
	private PushTask push;

	private Project(final String gitRepo, final GitRepo repo,
			final Config config) {
		this.gitRepo = gitRepo;
		this.repo = repo;
		this.config = config;
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

	public ConfigDatabase getConfigDatabase() {
		return config.getConfigDatabase();
	}

	public synchronized void setProjectPath(final String projectPath) {
		this.projectPath = projectPath;
	}

	public void initializeProject() {
		project = repo.getGitProject(projectPath);
		db = new FrontendDatabase(config.getDatabase(), gitRepo, projectPath);
		// if the project changes, the transferRepo doesn't just become
		// outdated, it becomes completely invalid, and we'll have to recreate
		// it completely.
		disposeTransferRepo();
	}

	public synchronized String getProjectPath() {
		return projectPath;
	}

	private void checkHaveProject() {
		if (project == null)
			throw new NoProjectException();
	}

	/**
	 * @return a {@link GitProject}, ie. with project path set and ready for
	 *         operations that need a project to be selected
	 */
	public synchronized GitProject getGitProject() {
		checkHaveProject();
		return project;
	}

	public FrontendDatabase getFrontendDatabase() {
		checkHaveProject();
		return db;
	}

	private void checkHaveTransferRepo() {
		if (!transferRepo.isUpToDate())
			throw new NeedCloneException();
	}

	public TransferRepo getTransferRepo() {
		checkHaveTransferRepo();
		return transferRepo;
	}

	public MetadataExtractor getMetadataExtractor() {
		checkHaveTransferRepo();
		return metadataExtractor;
	}

	public CloneTask createTransferRepo() {
		if (transferRepo == null || transferRepo.isDisposed()
				|| clone.isCancelled()) {
			// TransferRepo is invalid or nonexistent and cannot be reused.
			// create a new one.
			transferRepo = new TransferRepo(config.getRandomTempDir());
			metadataExtractor = new MetadataExtractor(transferRepo, project);
			clone = null;
		}

		if (clone == null || clone.isDone()) {
			// if the user is triggering the clone again after it has finished,
			// perform another clone in the same directory. the repo might have
			// changed and the user almost certainly wants to see this change in
			// the archived data.
			clone = new CloneTask(transferRepo, metadataExtractor,
					getGitProject(), db.getRefActions());
			clone.start();
		}
		return clone;
	}

	/**
	 * Called when the list of branches has changed, ie. after
	 * {@link FrontendDatabase#setRefAction(Ref, bwfdm.sara.project.RefAction.PublicationMethod, String)}
	 * . Needs to be called only once for multiple changes.
	 */
	public void invalidateTransferRepo() {
		if (clone != null && !clone.isDone()) {
			// clone is still running, but will be outdated once it finishes.
			// for now, just zap the TransferRepo completely. that way, we don't
			// have to worry about a previous clone still shutting down when the
			// next one starts
			// TODO we should try to keep at least the objects, they're big
			// this may not actually be necessary but the background thread may
			// still be removing the old directory. some steps block for several
			// seconds, so this is not a difficult race to trigger at all.
			clone.cancel();
			transferRepo = null;
			metadataExtractor = null;
			clone = null;
			return;
		}

		if (transferRepo != null)
			// CloneTask has finished, so the TransferRepo is outdated but
			// consistent. we can just "git pull" it.
			transferRepo.markOutdated();
		// or the user has never performed a clone anyway. nothing to do in that
		// case.
	}

	public void disposeTransferRepo() {
		// cancelling the CloneTask is always possible, even after it has
		// finished, and will always clean up the TransferRepo.
		if (clone != null)
			clone.cancel();
		clone = null;
		transferRepo = null;
	}

	public TaskStatus getInitStatus() {
		if (clone == null)
			return null;
		return clone.getStatus();
	}

	public void startPush() {
		if (push == null) {
			// FIXME this shouldn't have to be done HERE!
			// api.Push is a much better place...
			// FIXME archiveID should be dynamic
			final String archiveID = config.getConfigDatabase().getGitArchive();
			push = new PushTask(new ArchiveJob(this, archiveID));
		}
		push.start();
	}

	public void cancelPush() {
		if (push != null)
			push.cancel();
		push = null;
	}

	public PushTask getPushTask() {
		return push;
	}

	/**
	 * Called when publication metadata has changed, ie. after
	 * {@link FrontendDatabase#setLicense(Ref, String)},
	 * {@link FrontendDatabase#setMetadata(MetadataField, String)} etc, to mark
	 * the existing metadata copies as outdated. Needs to be called only once
	 * for multiple changes.
	 */
	public void invalidateMetadata() {
		// FIXME change to PushTaskConfig to this is safe to do
		// cancelPush();
		// FIXME is there anything else we need to do here?
	}

	public Config getConfig() {
		return config;
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
		final GitRepo repo = config.getConfigDatabase().newGitRepo(repoID);
		final Project project = new Project(repoID, repo, config);
		if (projectPath != null)
			project.projectPath = projectPath;
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
