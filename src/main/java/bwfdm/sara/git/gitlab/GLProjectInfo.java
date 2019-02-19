package bwfdm.sara.git.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.DataObject;
import bwfdm.sara.git.ProjectInfo;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
class GLProjectInfo implements DataObject<ProjectInfo> {
	/** default branch, <code>master</code> by default. */
	@JsonProperty("default_branch")
	String master;
	/** project display name / title */
	@JsonProperty("name")
	String title;
	/** project description */
	@JsonProperty("description")
	String description;
	/** full project path */
	@JsonProperty("path_with_namespace")
	String path;
	/** project name (last component of {@link #path}) */
	@JsonProperty("path")
	String name;
	/** repo clone URL */
	@JsonProperty("ssh_url_to_repo")
	String cloneURL;
	/** web root URL of project */
	@JsonProperty("web_url")
	String webURL;
	/** namespace containing the project */
	@JsonProperty("namespace")
	Namespace namespace;

	/**
	 * Used (and needed!) by Jackson to create an instance of the class when
	 * deserializing from JSON. Can be {@code private} because reflection
	 * doesn't care about visibility.
	 */
	@SuppressWarnings("unused")
	private GLProjectInfo() {
	}

	GLProjectInfo(final String title, final String description) {
		this.title = title;
		this.description = description;
	}

	@Override
	public ProjectInfo toDataObject() {
		return new ProjectInfo(path, title, description, master);
	}

	@Override
	public String toString() {
		return path;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class Namespace {
		/** full path to namespace */
		@JsonProperty("full_path")
		String path;
		/** internal (numeric) ID of namespace */
		@JsonProperty
		int id;
	}
}