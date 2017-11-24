package bwfdm.sara.git;

import java.util.HashMap;
import java.util.Map;

import bwfdm.sara.git.gitlab.GitLabArchiveRESTv4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArchiveRepoFactory {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, Class<? extends ArchiveRepo>> ADAPTERS = new HashMap<>();
	static {
		ADAPTERS.put("GitLabArchiveRESTv4", GitLabArchiveRESTv4.class);
	}

	private ArchiveRepoFactory() {
	}

	/**
	 * Creates a new {@link ArchiveRepo} instance.
	 * <p>
	 * Uses Jackson to construct the object from a {@code Map<String, String>}.
	 * For this to work, the constructor must be annotated with
	 * {@link JsonCreator}, and all its arguments must have {@link JsonProperty}
	 * annotation giving the argument's name in the JSON, ie. something like
	 * {@code @JsonProperty("url") String url}.
	 * 
	 * @return a new {@link ArchiveRepo}
	 */
	public static ArchiveRepo newArchiveRepo(final String adapter,
			final Map<String, String> args) {
		final Class<? extends ArchiveRepo> cls = ADAPTERS.get(adapter);
		// convertValue takes a Map<String, String> (or many, many other things)
		// and creates an object from it
		return MAPPER.convertValue(args, cls);
	}
}
