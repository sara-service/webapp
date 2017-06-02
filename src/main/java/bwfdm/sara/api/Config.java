package bwfdm.sara.api;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

public class Config {
	private static final String WEBROOT_ATTR = "sara.webroot";
	private static final String REPOCONFIG_ATTR = "repos.properties";

	public static String getWebRoot(final HttpSession session) {
		return getContextParam(session.getServletContext(), WEBROOT_ATTR);
	}

	public static Properties getGitRepoConfig(final HttpSession session) {
		final Properties props = new Properties();
		final String path = getContextParam(session.getServletContext(),
				REPOCONFIG_ATTR);
		try {
			props.load(new FileReader(path));
		} catch (final IOException e) {
			throw new ConfigurationException("cannot load repo list from "
					+ path, e);
		}
		return props;
	}

	private static String getContextParam(final ServletContext context,
			final String name) {
		final String attr = context.getInitParameter(name);
		if (attr == null)
			throw new ConfigurationException("missing context attribute "
					+ name);
		return attr;
	}

	@SuppressWarnings("serial")
	public static class ConfigurationException extends RuntimeException {
		public ConfigurationException(final String message,
				final Throwable cause) {
			super(message, cause);
		}

		public ConfigurationException(final String message) {
			super(message);
		}
	}
}
