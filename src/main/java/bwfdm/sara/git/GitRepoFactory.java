package bwfdm.sara.git;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import bwfdm.sara.git.gitlab.GitLabRESTv4;
import bwfdm.sara.project.Project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitRepoFactory {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, Class<? extends GitRepo>> ADAPTERS = new HashMap<>();
	static {
		ADAPTERS.put("GitLabRESTv4", GitLabRESTv4.class);
	}

	@JsonProperty
	public final String id;
	@JsonProperty
	public final String displayName;
	@JsonIgnore
	public final String adapter;

	public GitRepoFactory(final String id, final String displayName,
			final String adapter) {
		this.id = id;
		this.displayName = displayName;
		this.adapter = adapter;
	}

	/**
	 * Creates a new {@link GitRepo} instance. Meant to be called by
	 * {@link Project#createInstance(HttpSession, String)} only!
	 * <p>
	 * Uses Jackson to construct the object from a {@code Map<String, String>}.
	 * For this to work, the constructor must be annotated with
	 * {@link JsonCreator}, and all its arguments must have {@link JsonProperty}
	 * annotation giving the argument's name in the JSON, ie. something like
	 * {@code @JsonProperty("url") String url}.
	 * 
	 * @return a new {@link GitRepo}
	 */
	public GitRepo newGitRepo(final Map<String, String> args) {
		final Class<? extends GitRepo> cls = ADAPTERS.get(adapter);
		// convertValue takes a Map<String, String> (or many, many other things)
		// and creates an object from it
		return MAPPER.convertValue(args, cls);
	}
}
