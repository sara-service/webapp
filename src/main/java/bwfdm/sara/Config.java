package bwfdm.sara;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.ServletContextAware;

import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.git.gitlab.GitLabArchive;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Config implements ServletContextAware {
	private static final String WEBROOT_ATTR = "sara.webroot";
	private static final String TEMPDIR_ATTR = "temp.dir";
	private static final String REPOCONFIG_ATTR = "repos.config";
	private static final String ARCHIVE_CONFIG_ATTR = "archive.config";

	private static final SecureRandom RNG = new SecureRandom();
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final ObjectMapper mapper;
	@Autowired
	private DataSource db;
	private String webroot;
	private File temproot;
	private Map<String, GitRepoFactory> repoConfig;
	private ArchiveRepo archiveRepo;

	/**
	 * This class must be instantiated by Spring because it relies on injected
	 * components.
	 */
	private Config() {
		mapper = new ObjectMapper();
		// these are nonstandard but make JSON files much more human-friendly
		mapper.enable(Feature.ALLOW_COMMENTS);
		mapper.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
	}

	@Override
	public void setServletContext(final ServletContext servletContext) {
		// this.servletContext = servletContext;
		webroot = getContextParam(servletContext, WEBROOT_ATTR);
		repoConfig = readRepoConfig(servletContext);
		archiveRepo = readArchiveConfig(servletContext);

		temproot = getTempRoot(servletContext);
		temproot.mkdirs(); // failure harmless if already there
		if (!temproot.isDirectory())
			throw new RuntimeException("temp directory "
					+ temproot.getAbsolutePath() + " cannot be created");
	}

	private static String getContextParam(final ServletContext context,
			final String name) {
		final String attr = context.getInitParameter(name);
		if (attr == null)
			throw new ConfigurationException("missing context parameter "
					+ name);
		return attr;
	}

	private Map<String, GitRepoFactory> readRepoConfig(
			final ServletContext servletContext) {
		final String repoConfig = getContextParam(servletContext,
				REPOCONFIG_ATTR);
		try {
			return mapper.readValue(new File(repoConfig),
					new TypeReference<Map<String, GitRepoFactory>>() {
					});
		} catch (final IOException e) {
			throw new RuntimeException("cannot parse " + repoConfig, e);
		}
	}

	private GitLabArchive readArchiveConfig(final ServletContext servletContext) {
		final String archiveConfig = getContextParam(servletContext,
				ARCHIVE_CONFIG_ATTR);
		try {
			return mapper.readValue(new File(archiveConfig),
					new TypeReference<GitLabArchive>() {
					});
		} catch (final IOException e) {
			throw new RuntimeException("cannot parse " + archiveConfig, e);
		}
	}

	private static File getTempRoot(final ServletContext servletContext) {
		final File servletTemp = (File) servletContext
				.getAttribute(ServletContext.TEMPDIR);
		final String tempPath = servletContext.getInitParameter(TEMPDIR_ATTR);
		// if parameter missing, use servlet container's tempdir
		if (tempPath == null || tempPath.isEmpty())
			return servletTemp;
		// if parameter is an absolute path, use it as is
		final File absPath = new File(tempPath);
		if (absPath.isAbsolute())
			return absPath;
		// else resolve it relative to the servlet container's tempdir
		return new File(servletTemp, tempPath);
	}

	public JdbcTemplate newJdbcTemplate() {
		return new JdbcTemplate(db);
	}

	public String getWebRoot() {
		return webroot;
	}

	public Map<String, GitRepoFactory> getRepoConfig() {
		return repoConfig;
	}

	public ArchiveRepo getArchiveRepo() {
		return archiveRepo;
	}

	/**
	 * Creates a randomly-named temporary directory. The same directory is never
	 * returned twice. The directory will exists on return; delete it after use.
	 * 
	 * @return a unique temp directory
	 */
	public File getRandomTempDir() {
		// 80 bits is enough that collisions happen only every 2^40 > 10^12
		// operations, ie. which in practice means they never happen.
		final String name = "r" + getRandomID();
		final File temp = new File(temproot, name);
		temp.mkdirs(); // ignore error; it may well exist already
		if (!temp.isDirectory())
			throw new RuntimeException("failed to create directory "
					+ temp.getAbsolutePath());
		return temp;
	}

	/**
	 * Creates a new random identifier. The same string is never returned twice.
	 * 
	 * @return a unique string
	 */
	public static String getRandomID() {
		return new BigInteger(80, RNG).toString(Character.MAX_RADIX);
	}

	/**
	 * Creates a temporary directory for the given key. The same directory is
	 * returned for the same key (obviously), and no two different keys ever
	 * share the same directory. The directory will exists on return; delete it
	 * after use.
	 * 
	 * @param key
	 *            one or more strings
	 * @return the unique temp directory for that key
	 */
	public File getTempDir(final String... key) {
		if (key.length == 0)
			throw new IllegalArgumentException("empty key");

		// MD5-based naming so the keys don't have to be valid pathnames. this
		// is used for project paths which do tend to include slashes.
		final StringBuilder hash = new StringBuilder();
		for (final String k : key) {
			if (hash.length() > 0)
				hash.append('.');
			DigestUtils.appendMd5DigestAsHex(k.getBytes(UTF8), hash);
		}

		final File temp = new File(temproot, hash.toString());
		temp.mkdirs(); // ignore error; it may well exist already
		if (!temp.isDirectory())
			throw new RuntimeException("failed to create directory "
					+ temp.getAbsolutePath());
		return temp;
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
