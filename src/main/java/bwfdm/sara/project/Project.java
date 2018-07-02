package bwfdm.sara.project;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.publication.db.PublicationDatabase;
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

	/**
	 * Changes the project path, re-initializing this project. This is for use
	 * in the {@link Authorization authorization logic} and nowhere else.
	 */
	public synchronized void setProjectPath(final String projectPath) {
		if (this.projectPath != null && this.projectPath.equals(projectPath))
			return;

		this.projectPath = projectPath;
		project = null;
		db = null;
		disposeTransferRepo();
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
	
	public PublicationDatabase getPublicationDatabase() {
		return config.getPublicationDatabase();
	}

	private void checkHaveTransferRepo() {
		if (transferRepo == null || !transferRepo.isUpToDate())
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
			metadataExtractor = new MetadataExtractor(transferRepo, repo,
					project);
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
		invalidateMetadata();
		// cancelling the CloneTask is always possible, even after it has
		// finished, and will always clean up the TransferRepo.
		if (clone != null)
			clone.cancel();
		clone = null;
		transferRepo = null;
		metadataExtractor = null;
	}

	public TaskStatus getInitStatus() {
		if (clone == null)
			return null;
		return clone.getStatus();
	}

	public void startPush(final ArchiveJob job) {
		// if a different ArchiveJob is running, kill it
		// FIXME shouldn't we just throw an exception?
		// this should never happen!
		if (push != null && !push.getArchiveJob().equals(job))
			cancelPush();

		if (push == null) {
			final ArchiveRepo archive = config.getConfigDatabase()
					.newGitArchive(job.archiveUUID.toString());
			final PublicationDatabase pubDB = config.getPublicationDatabase();
			push = new PushTask(job, archive, pubDB);
		}
		push.start();
	}

	public ArchiveJob getArchiveJob() {
		// FIXME archiveID should be dynamic
		final String archiveID = config.getConfigDatabase().getGitArchive();
		return new ArchiveJob(this, archiveID);
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
		// this allows the user to trigger the clone again
		// FIXME but it also crashes if (s)he returns after push finished!
		cancelPush();
	}

	/**
	 * @return <code>true</code> if the archiving part of the project is
	 *         completed, ie. if {@link PushTask#isDone()} or he push wasn't
	 *         started yet
	 */
	public boolean isDone() {
		if (push == null)
			return false;
		return push.isDone();
	}

	/**
	 * Get the {@link Project} instance associated with the given
	 * {@link HttpSession}.
	 * 
	 * @throws ProjectCompletedException
	 *             if the project {@link #isDone() is completed}
	 */
	public static Project getInstance(final HttpSession session) {
		final Project project = getCompletedInstance(session);
		if (project.isDone())
			throw new ProjectCompletedException(
					project.getPushTask().getItemUUID());
		return project;
	}

	/**
	 * Get the {@link Project} instance associated with the given
	 * {@link HttpSession}, even if that {@link Project} is already completed.
	 * Only intended for logic that needs to check {@link #isDone()} manually,
	 * or to get auth info from the completed project. For general API stuff,
	 * use {@link #getInstance(HttpSession)}!
	 * 
	 * @throws NoSessionException
	 *             if the session doesn't have {@link Project} associated with
	 *             it
	 * @return the {@link Project} instance
	 */
	public static Project getCompletedInstance(final HttpSession session) {
		Project project = (Project) session.getAttribute(PROJECT_ATTR);
		if (project == null)
			throw new NoSessionException();
		return project;
	}

	/**
	 * Convenience method for calling {@link #getInstance(HttpSession)} then
	 * {@link Project#getGitProject()}.
	 */
	public static GitProject getGitProject(final HttpSession session) {
		return getInstance(session).getGitProject();
	}

	public static boolean hasInstance(final HttpSession session) {
		return (Project) session.getAttribute(PROJECT_ATTR) != null;
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
	public static class ProjectCompletedException extends RuntimeException {
		public final UUID itemID;

		private ProjectCompletedException(final UUID itemID) {
			super("archiving project already completed");
			this.itemID = itemID;
		}
	}

	@SuppressWarnings("serial")
	public static class NeedCloneException extends RuntimeException {
		private NeedCloneException() {
			super("need to clone the repository first");
		}
	}
}
