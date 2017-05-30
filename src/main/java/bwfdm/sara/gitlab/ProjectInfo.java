package bwfdm.sara.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectInfo {
	/** default branch, <code>master</code> by default. */
	@JsonProperty("default_branch")
	public String master;
	@JsonProperty("name")
	public String name;
	@JsonProperty("description")
	public String description;
}