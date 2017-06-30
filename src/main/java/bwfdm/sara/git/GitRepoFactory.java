package bwfdm.sara.git;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import bwfdm.sara.Config;
import bwfdm.sara.Config.ConfigurationException;
import bwfdm.sara.ProjectFactory;

public class GitRepoFactory {
	public static GitRepo getInstance(final HttpSession session) {
		return ProjectFactory.getInstance(session).getGitRepo();
	}

	/**
	 * Creates a new {@link GitRepo} instance, overwriting the previous one.
	 * Meant to be called by
	 * {@link ProjectFactory#createInstance(HttpSession, String)} only!
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

		// collect all properties that start with the gitRepo's name
		final Properties args = new Properties();
		final String prefix = gitRepo + ".";
		for (final String name : config.stringPropertyNames())
			if (name.startsWith(prefix)) {
				final String localName = name.substring(prefix.length());
				args.setProperty(localName, config.getProperty(name));
			}

		// try to instantiate the concrete gitRepo class
		String className = args.getProperty("class");
		if (className == null)
			className = config.getProperty("default.class");
		try {
			return (GitRepo) Class.forName(className)
					.getConstructor(Properties.class).newInstance(args);
		} catch (final ClassNotFoundException | InstantiationException
				| IllegalAccessException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ConfigurationException("cannot instantiate " + className
					+ " for " + gitRepo, e);
		}
	}
}
