package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.ProjectInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
class GLProjectInfo implements GLDataObject<ProjectInfo> {
	/** default branch, <code>master</code> by default. */
	@JsonProperty("default_branch")
	String master;
	@JsonProperty("name")
	String name;
	@JsonProperty("description")
	String description;
	@JsonProperty("path_with_namespace")
	String path;

	/**
	 * Used (and needed!) by Jackson to create an instance of the class when
	 * deserializing from JSON. Can be {@code private} because reflection
	 * doesn't care about visibility.
	 */
	@SuppressWarnings("unused")
	private GLProjectInfo() {
	}

	GLProjectInfo(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public ProjectInfo toDataObject() {
		return new ProjectInfo(path, name, description);
	}
}