package bwfdm.sara.git;

import javax.servlet.http.HttpSession;

import bwfdm.sara.project.Project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = false)
public class GitRepoFactory {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@JsonProperty
	private String id;
	@JsonProperty
	private String displayName;
	@JsonProperty("class")
	private String className;
	@JsonProperty
	private JsonNode args;

	/** used only by Jackson via reflection */
	private GitRepoFactory() {
	}

	public String getID() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Creates a new {@link GitRepo} instance. Meant to be called by
	 * {@link Project#createInstance(HttpSession, String)} only!
	 * <p>
	 * Uses Jackson to construct the object from JSON. For this to work, the
	 * constructor must be annotated with {@link JsonCreator}, and all its
	 * arguments must have {@link JsonProperty} annotation giving the argument's
	 * name in the JSON, ie. something like
	 * {@code @JsonProperty("url") String url}.
	 * 
	 * @return a new {@link GitRepo}
	 */
	public GitRepo newGitRepo() {
		try {
			@SuppressWarnings("unchecked")
			final Class<GitRepo> cls = (Class<GitRepo>) Class
					.forName(className);
			// convertValue takes a raw JSON tree and creates an object from it
			return MAPPER.convertValue(args, cls);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("cannot instantiate " + id, e);
		}
	}
}
