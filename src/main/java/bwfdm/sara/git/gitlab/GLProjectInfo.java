package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.ProjectInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLProjectInfo implements ProjectInfo {
	/** default branch, <code>master</code> by default. */
	@JsonProperty("default_branch")
	String master;
	@JsonProperty("name")
	private String name;
	@JsonProperty("description")
	private String description;

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}
}