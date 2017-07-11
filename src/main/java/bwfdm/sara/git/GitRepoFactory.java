package bwfdm.sara.git;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import bwfdm.sara.Config;
import bwfdm.sara.project.Project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = false)
public class GitRepoFactory {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@JsonProperty
	private String displayName;
	@JsonProperty("class")
	private String className;
	@JsonProperty
	private JsonNode args;

	/** used only by Jackson via reflection */
	private GitRepoFactory() {
	}

	public String getDisplayName() {
		return displayName;
	}

	public GitRepo newGitRepo() throws ClassNotFoundException {
		@SuppressWarnings("unchecked")
		final Class<GitRepo> cls = (Class<GitRepo>) Class.forName(className);
		// use Jackson to construct the object from JSON. for this to work, the
		// constructor must be annotated with @JsonCreator, and all its
		// arguments must have @JsonProperty annotation giving the argument's
		// name in JSON, ie. something like @JsonProperty("url")
		return MAPPER.convertValue(args, cls);
	}

	/**
	 * Creates a new {@link GitRepo} instance, overwriting the previous one.
	 * Meant to be called by {@link Project#createInstance(HttpSession, String)}
	 * only!
	 * 
	 * @param gitRepo
	 *            ID of the gitlab instance
	 * @param config
	 *            the global {@link Config} object (use {@link Autowired} to
	 *            have Spring inject it)
	 * 
	 * @return a new {@link GitRepo}
	 */
	public static GitRepo createInstance(final String gitRepo,
			final Config config) {
		try {
			return config.getRepoConfig().get(gitRepo).newGitRepo();
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("cannot instantiate " + gitRepo, e);
		}
	}
}
