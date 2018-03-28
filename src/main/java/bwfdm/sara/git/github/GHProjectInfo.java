package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.ProjectInfo;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
class GHProjectInfo implements GHDataObject<ProjectInfo> {
	/** default branch, <code>master</code> by default. */
	@JsonProperty("default_branch")
	String master;
	/** project description */
	@JsonProperty("description")
	String description;
	/** full project path */
	@JsonProperty("full_name")
	String path;
	/** full project path */
	@JsonProperty("url")
	String apiURL;
	/** project name (last component of {@link #path}) */
	@JsonProperty("name")
	String name;
	/** repo clone URL */
	@JsonProperty("ssh_url")
	String cloneURL;
	/** web root URL of project */
	@JsonProperty("html_url")
	String webURL;

	/**
	 * Used (and needed!) by Jackson to create an instance of the class when
	 * deserializing from JSON. Can be {@code private} because reflection
	 * doesn't care about visibility.
	 */
	@SuppressWarnings("unused")
	private GHProjectInfo() {
	}

	GHProjectInfo(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public ProjectInfo toDataObject() {
		return new ProjectInfo(path, name, description, master);
	}
}