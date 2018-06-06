package bwfdm.sara.project;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.api.Authorization;
import bwfdm.sara.publication.db.PublicationDatabase;

public class PublicationSession {
	private static final String PROJECT_ATTR = PublicationSession.class.getCanonicalName();

	private final Config config;
	private final String email;
	private final String gitRepo;
	private final UUID item;

	private PublicationSession(final String gitRepo, final String email,
			final UUID item,
			final Config config) {
		this.gitRepo = gitRepo;
		this.email = email;
		this.item = item;
		this.config = config;
	}

	public String getRepoID() {
		return gitRepo;
	}

	public String getGitRepoEmail() {
		return email;
	}

	public UUID getArchiveItem() {
		return item;
	}

	public PublicationDatabase getPublicationDatabase() {
		return config.getPublicationDatabase();
	}

	public static PublicationSession getInstance(final HttpSession session) {
		final PublicationSession repo = (PublicationSession) session.getAttribute(PROJECT_ATTR);
		if (repo == null)
			throw new NoSessionException();
		return repo;
	}

	public static boolean hasInstance(final HttpSession session) {
		return session.getAttribute(PROJECT_ATTR) != null;
	}

	/**
	 * Creates a new {@link PublicationSession} instance, overwriting the
	 * previous one. Meant to be called by the {@link Authorization login /
	 * session creation code} only!
	 *
	 * @param session
	 *            the user's {@link HttpSession}
	 * @param gitRepo
	 *            ID of the git repo, to qualify the email addressname
	 * @param email
	 *            the user's email address in that git repo
	 * @param item
	 *            UUID of the archival item which is about to be published (must
	 *            not be <code>null</code>)
	 * @param config
	 *            the global {@link Config} object (use {@link Autowired} to
	 *            have Spring inject it)
	 */
	public static PublicationSession createInstance(final HttpSession session,
			final String repoID, final String email, final UUID item,
			final Config config) {
		final PublicationSession project = new PublicationSession(repoID, email,
				item, config);
		session.setAttribute(PROJECT_ATTR, project);
		return project;
	}

	public static PublicationSession createInstance(final HttpSession session,
			final Project project, final UUID item) {
		// FIXME use project.getGitRepo().getUserInfo() instead
		final String email = "root@local.host";
		return createInstance(session, project.getRepoID(), email, item,
				project.getConfig());
	}

	@SuppressWarnings("serial")
	public static class NoSessionException extends RuntimeException {
		private NoSessionException() {
			super("session expired or not found");
		}
	}

	@SuppressWarnings("serial")
	public static class NoItemException extends RuntimeException {
		private NoItemException() {
			super("no project selected");
		}
	}
}
