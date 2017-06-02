package bwfdm.sara.git;

import javax.servlet.http.HttpSession;

import bwfdm.sara.api.Config;
import bwfdm.sara.git.gitlab.GitLab;

public class GitRepoFactory {
	private static final String GITREPO_ATTR = GitRepo.class.getCanonicalName();

	public static GitRepo getInstance(final HttpSession session) {
		final GitRepo repo = (GitRepo) session.getAttribute(GITREPO_ATTR);
		if (repo == null)
			throw new IllegalStateException(
					"no repo / project found. session expired?");
		return repo;
	}

	public static boolean hasInstance(final HttpSession session) {
		return session.getAttribute(GITREPO_ATTR) != null;
	}

	/**
	 * Creates a new {@link GitRepo} instance, overwriting the previous one.
	 * Meant to be called by the login / session creation code only!
	 * 
	 * @param session
	 *            the user's {@link HttpSession}
	 * @param gitRepo
	 *            ID of the gitlab instance
	 * @return
	 */
	public static GitRepo createInstance(final HttpSession session,
			final String gitRepo) {
		// TODO should read root URL and secrets from config here
		final GitLab repo = new GitLab(gitRepo, Config.GITLAB, Config.APP_ID,
				Config.APP_SECRET);
		session.setAttribute(GITREPO_ATTR, repo);
		return repo;
	}
}
