package bwfdm.sara.git;

import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitLab project info. */
public interface ProjectInfo {
	@JsonProperty("name")
	public String getName();

	@JsonProperty("description")
	public String getDescription();
}