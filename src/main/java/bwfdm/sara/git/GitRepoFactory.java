package bwfdm.sara.git;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import bwfdm.sara.api.Config;

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
		final Properties config = Config.getGitRepoConfig(session);

		final Properties args = new Properties();
		final String prefix = gitRepo + ".";
		for (final String name : config.stringPropertyNames())
			if (name.startsWith(prefix)) {
				final String localName = name.substring(prefix.length());
				args.setProperty(localName, config.getProperty(name));
			}

		String className = args.getProperty("class");
		if (className == null)
			className = config.getProperty("default.class");
		final GitRepo repo;
		try {
			repo = (GitRepo) Class.forName(className)
					.getConstructor(String.class, Properties.class)
					.newInstance(gitRepo, args);
		} catch (final ClassNotFoundException | InstantiationException
				| IllegalAccessException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new Config.ConfigurationException("cannot instantiate "
					+ className + " for " + gitRepo, e);
		}
		session.setAttribute(GITREPO_ATTR, repo);
		return repo;
	}
}
