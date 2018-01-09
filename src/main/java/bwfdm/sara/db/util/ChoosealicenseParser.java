package bwfdm.sara.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Parses a file from choosealicense.com's {@code _licenses} directory into a
 * {@link LicenseMetadata} object. Not thread safe.
 */
class ChoosealicenseParser {
	private static final String YAML_OBJECT_SEPARATOR = "---";

	private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private final StringBuilder yaml = new StringBuilder();
	private final StringBuilder text = new StringBuilder();
	private StringBuilder curBuffer;

	public LicenseMetadata parse(final File file) throws IOException {
		try (BufferedReader data = new BufferedReader(new FileReader(file))) {
			return parse(data);
		}
	}

	public LicenseMetadata parse(final BufferedReader data) throws IOException {
		reset();
		append(data);
		final LicenseMetadata meta = mapper.readValue(yaml.toString(),
				LicenseMetadata.class);
		meta.setFullText(text.toString());
		return meta;
	}

	private void reset() {
		yaml.setLength(0);
		text.setLength(0);
		curBuffer = null;
	}

	private void append(final BufferedReader data) throws IOException {
		while (true) {
			final String line = data.readLine();
			if (line == null)
				if (curBuffer != text)
					throw new IllegalArgumentException("truncated file");
				else
					break;
			append(line);
		}
	}

	/**
	 * Brute-force splitter for the weird YAML-plus-text format used by the
	 * choosealicense.com license database files. At least it has the desirable
	 * side effect of canonicalizing line endings...
	 * <p>
	 * This could also be phrased as: YAML-Jackson just doesn't implement the
	 * {@link http://www.yaml.org/spec/1.2/spec.html#id2760395 document start
	 * directives} part of the YAML spec.
	 */
	private void append(final String line) {
		if (curBuffer != null)
			curBuffer.append(line).append('\n');

		if (line.equals(YAML_OBJECT_SEPARATOR))
			if (curBuffer == null)
				curBuffer = yaml;
			else if (curBuffer == yaml)
				curBuffer = text;
	}
}
