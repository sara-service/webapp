package bwfdm.sara;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.sql.Driver;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.ServletContextAware;

import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.publication.db.PublicationDatabase;

@Component
public class Config implements ServletContextAware {
	/**
	 * Name of the properties file that Spring loads on startup. Use for calling
	 * {@link #Config(String)} in testcases and support scripts.
	 */
	public static final String SPRING_APPLICATION_CONFIG_FILE = "application.properties";

	private static final String WEBROOT_ATTR = "sara.webroot";
	private static final String TEMPDIR_ATTR = "temp.dir";
	private static final String CONTEXT_PARAM_PREFIX = "server.context_parameters.";
	private static final String DATASOURCE_PREFIX = CONTEXT_PARAM_PREFIX
			+ "spring.datasource.";

	private static final SecureRandom RNG = new SecureRandom();
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final Properties props;
	private DataSource db;
	private ServletContext context;
	private String webroot;
	private File temproot;
	private ConfigDatabase configDB;
	private PublicationDatabase pubDB;

	/**
	 * Constructor used by Spring, along with
	 * {@link #setServletContext(ServletContext)} and injection for the database
	 * config.
	 */
	@SuppressWarnings("unused")
	private Config() {
		this.props = null;
	}

	/**
	 * Constructor used <b>for testing only</>, when context parameters are
	 * simply loaded from a {@link Properties} object. Use
	 * {@code @Autowired private Config config;} everywhere else!
	 * 
	 * @param props
	 *            {@link Properties} that has been
	 *            {@link Properties#load(InputStream) loaded} from a file
	 */
	public Config(final Properties props) throws IOException {
		this.props = props;
		setServletContext(null);
		setDataSource(createDataSource(props));
	}

	public static DataSource createDataSource(final Properties props)
			throws IOException {
		final String klass = props
				.getProperty(DATASOURCE_PREFIX + "driver-class-name");
		Driver driver;
		try {
			driver = (Driver) Class.forName(klass).newInstance();
		} catch (final ReflectiveOperationException e) {
			throw new IOException("database driver not found", e);
		}

		final String url = props.getProperty(DATASOURCE_PREFIX + "url");
		final String user = props.getProperty(DATASOURCE_PREFIX + "username");
		final String pass = props.getProperty(DATASOURCE_PREFIX + "password");
		return new SimpleDriverDataSource(driver, url, user, pass);
	}

	@Autowired
	public void setDataSource(final DataSource db) {
		this.db = db;
		configDB = new ConfigDatabase(db);
		pubDB = new PublicationDatabase(db);
	}

	@Override
	public void setServletContext(final ServletContext servletContext) {
		context = servletContext;
		webroot = getContextParam(WEBROOT_ATTR);

		temproot = getTempRoot();
		temproot.mkdirs(); // failure harmless if already there
		if (!temproot.isDirectory())
			throw new RuntimeException("temp directory "
					+ temproot.getAbsolutePath() + " cannot be created");
	}

	public ConfigDatabase getConfigDatabase() {
		return configDB;
	}

	public PublicationDatabase getPublicationDatabase() {
		return pubDB;
	}

	private String getContextParam(final String name) {
		final String attr;
		if (!testMode())
			attr = context.getInitParameter(name);
		else
			// for testing, just read from the properties file directly.
			// add prefix so we can use application.properties directly.
			attr = props.getProperty(CONTEXT_PARAM_PREFIX + name);
		if (attr == null)
			throw new ConfigurationException("missing context parameter "
					+ name);
		return attr;
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
	 * Creates a randomly-named temporary directory. The same directory is never
	 * returned twice. The directory will exists on return; delete it after use.
	 * 
	 * @return a unique temp directory
	 */
	public File getRandomTempDir() {
		// 80 bits is enough that collisions happen only every 2^40 > 10^12
		// operations, which in practice is the same as "never".
		final String name = "r" + getRandomID();
		final File temp = new File(temproot, name);
		temp.mkdirs(); // ignore errors; we only need it to exist afterwards
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
