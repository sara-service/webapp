package bwfdm.sara;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.ServletContextAware;

import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.git.gitlab.GitLabArchive;
import bwfdm.sara.publication.PublicationRepositoryFactory;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Config implements ServletContextAware {
	private static final String WEBROOT_ATTR = "sara.webroot";
	private static final String TEMPDIR_ATTR = "temp.dir";
	private static final String REPOCONFIG_ATTR = "repos.config";
	private static final String IRCONFIG_ATTR = "publish.config";
	private static final String ARCHIVE_CONFIG_ATTR = "archive.config";

	private static final SecureRandom RNG = new SecureRandom();
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final ObjectMapper mapper;
	private final Properties props;
	private DataSource db;
	private ServletContext context;
	private String webroot;
	private File temproot;
	private List<GitRepoFactory> repoConfig;
	private List<PublicationRepositoryFactory> irConfig;
	private ArchiveRepo archiveRepo;
	private ConfigDatabase configDB;

	private Config(final Properties props) {
		this.props = props;
		mapper = new ObjectMapper();
		// these are nonstandard but make JSON files much more human-friendly
		mapper.enable(Feature.ALLOW_COMMENTS);
		mapper.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
	}

	/**
	 * Constructor used by Spring, along with
	 * {@link #setServletContext(ServletContext)} and injection for the database
	 * config.
	 */
	@SuppressWarnings("unused")
	private Config() {
		this((Properties) null);
	}

	/**
	 * Constructor used <b>for testing only</>, when context parameters are
	 * simply loaded from a properties file (or resource). Use
	 * {@code @Autowired private Config config;} everywhere else!
	 * 
	 * @param contextParams
	 *            either a {@link FileInputStream} for a properties file, or the
	 *            one returned by {@link Class#getResourceAsStream(String)} when
	 *            reading from a resource
	 */
	public Config(final InputStream contextParams) throws IOException {
		this(new Properties());
		props.load(contextParams);
		setServletContext(null);
	}

	/**
	 * Sets the {@link DataSource} to use for database access. Can be uses for
	 * testing but is also used by Spring to inject the configured database.
	 */
	@Autowired
	public void setDataSource(final DataSource db) {
		this.db = db;
		configDB = new ConfigDatabase(db);
	}

	@Override
	public void setServletContext(final ServletContext servletContext) {
		context = servletContext;
		webroot = getContextParam(WEBROOT_ATTR);
		repoConfig = readRepoConfig(REPOCONFIG_ATTR,
				new TypeReference<List<GitRepoFactory>>() {
				});
		irConfig = readRepoConfig(IRCONFIG_ATTR,
				new TypeReference<List<PublicationRepositoryFactory>>() {
				});
		archiveRepo = readArchiveConfig();

		temproot = getTempRoot();
		temproot.mkdirs(); // failure harmless if already there
		if (!temproot.isDirectory())
			throw new RuntimeException("temp directory "
					+ temproot.getAbsolutePath() + " cannot be created");
	}

	public ConfigDatabase getConfigDatabase() {
		return configDB;
	}

	private String getContextParam(final String name) {
		final String attr;
		if (!testMode())
			attr = context.getInitParameter(name);
		else
			// for testing, just read from the properties file directly.
			// add prefix so we can use application.properties directly.
			attr = props.getProperty("server.context_parameters." + name);
		if (attr == null)
			throw new ConfigurationException("missing context parameter "
					+ name);
		return attr;
	}

	private <T> List<T> readRepoConfig(final String attribute,
			final TypeReference<List<T>> type) {
		final String repoConfig = getContextParam(attribute);
		try {
			return mapper.readValue(new File(repoConfig), type);
		} catch (final IOException e) {
			throw new RuntimeException("cannot parse " + repoConfig, e);
		}
	}

	private GitLabArchive readArchiveConfig() {
		final String archiveConfig = getContextParam(ARCHIVE_CONFIG_ATTR);
		try {
			return mapper.readValue(new File(archiveConfig),
					new TypeReference<GitLabArchive>() {
					});
		} catch (final IOException e) {
			throw new RuntimeException("cannot parse " + archiveConfig, e);
		}
	}

	private File getTempRoot() {
		if (testMode())
			return new File("temp");

		final File servletTemp = (File) context
				.getAttribute(ServletContext.TEMPDIR);
		final String tempPath = context.getInitParameter(TEMPDIR_ATTR);
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

	private boolean testMode() {
		return context == null && props != null;
	}

	public DataSource getDatabase() {
		return db;
	}

	public String getWebRoot() {
		return webroot;
	}

	/**
	 * @param id
	 *            repo name used in {@code gitlabs.json}
	 * @return the {@link GitRepoFactory} for the name {@link GitRepo}
	 */
	public GitRepoFactory getGitRepoFactory(final String id) {
		for (final GitRepoFactory r : repoConfig)
			if (r.getID().equals(id))
				return r;
		throw new NoSuchElementException("no git repo named " + id);
	}

	/**
	 * @param id
	 *            name of institutional repository in {@code publish.json}
	 * @return the {@link GitRepoFactory} for the name {@link GitRepo}
	 */
	public PublicationRepositoryFactory getPublicationRepositoryFactory(
			final String id) {
		for (final PublicationRepositoryFactory r : irConfig)
			if (r.getID().equals(id))
				return r;
		throw new NoSuchElementException("no publication repository named "
				+ id);
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
