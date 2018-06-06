package bwfdm.sara.git;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.git.gitlab.GitLabArchiveRESTv4;

public class ArchiveRepoFactory {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, Class<? extends ArchiveRepo>> ADAPTERS = new HashMap<>();
	static {
		ADAPTERS.put("GitLabArchiveRESTv4", GitLabArchiveRESTv4.class);
	}

	@JsonProperty("id")
	public final String id;
	@JsonProperty("display_name")
	public final String displayName;
	@JsonProperty("logo")
	public final String logoURL;
	private final String adapter;

	/**
	 * Note: This is called indirectly by {@link ConfigDatabase#getGitArchive()}
	 * and related functions. The {@link JsonProperty} field names correspond
	 * directly to database column names.
	 */
	@JsonCreator
	public ArchiveRepoFactory(@JsonProperty("uuid") final String id,
			@JsonProperty("display_name") final String displayName,
			@JsonProperty("logo_url") final String logoURL,
			@JsonProperty("adapter") final String adapter) {
		this.id = id;
		this.displayName = displayName;
		this.logoURL = logoURL;
		this.adapter = adapter;
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
	public ArchiveRepo newArchiveRepo(final Map<String, String> args) {
		final Class<? extends ArchiveRepo> cls = ADAPTERS.get(adapter);
		// convertValue takes a Map<String, String> (or many, many other things)
		// and creates an object from it
		return MAPPER.convertValue(args, cls);
	}
}
