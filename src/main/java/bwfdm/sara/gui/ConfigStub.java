package bwfdm.sara.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.publication.PublicationRepositoryDeprecated;
import bwfdm.sara.publication.PublicationRepositoryFactory;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Stub to read {@link PublicationRepository} config fro JSON, to keep
 * {@link MainInt} running.
 * 
 * @deprecated config should be read from database instead
 */
@Deprecated
class ConfigStub {
	private final ObjectMapper mapper;
	private List<PublicationRepositoryFactory> irConfig;

	public ConfigStub() throws IOException {
		mapper = new ObjectMapper();
		// these are nonstandard but make JSON files much more human-friendly
		mapper.enable(Feature.ALLOW_COMMENTS);
		mapper.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);

		final File file = new File("publish.json");
		try {
			irConfig = mapper.readValue(file,
					new TypeReference<List<PublicationRepositoryFactory>>() {
					});
		} catch (final IOException e) {
			throw new RuntimeException("cannot parse " + file, e);
		}
	}

	/**
	 * @param id
	 *            name of institutional repository in {@code publish.json}
	 * @return the {@link GitRepoFactory} for the name {@link GitRepo}
	 */
	public PublicationRepositoryFactory getPublicationRepositoryFactory(
			final String id) {
		for (final PublicationRepositoryFactory r : irConfig)
			if (r.dao.display_name.equals(id))
				return r;
		throw new NoSuchElementException("no publication repository named "
				+ id);
	}
}
