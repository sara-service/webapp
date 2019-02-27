package bwfdm.sara;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/** {@link Config} stub for unit tests. */
public class UnitTestConfig extends Config {
	private static final String CONTEXT_PARAM_PREFIX = "server.context_parameters.";
	private static final SARAVersion VERSION = new SARAVersion("UNIT-TEST",
			"UNIT-TEST", "0000000000000000000000000000000000000000", 0);

	private final Properties props;
	private final File tempRoot;

	public UnitTestConfig(final Properties props) throws IOException {
		this.tempRoot = createTempDirectory();
		this.props = props;
		setServletContext(null); // this may break stuff
		setDataSource(createDataSource(props));
	}

	@Override
	protected String getContextParam(final String name) {
		// for testing, just read from the properties file directly.
		// add prefix so we can use application.properties directly.
		final String attr = props.getProperty(CONTEXT_PARAM_PREFIX + name);
		if (attr == null)
			throw new ConfigurationException(
					"missing context parameter " + name);
		return attr;
	}

	@Override
	protected File getTempRoot() {
		return tempRoot;
	}

	@Override
	public SARAVersion getVersion() {
		return VERSION;
	}

	public static File createTempDirectory() throws IOException {
		return Files.createTempDirectory("sara-").toFile();
	}
}
