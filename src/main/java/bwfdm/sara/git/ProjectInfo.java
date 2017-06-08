package bwfdm.sara.git;

import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitLab project info. */
public final class ProjectInfo {
	@JsonProperty("name")
	public final String name;

	@JsonProperty("description")
	public final String description;

	public ProjectInfo(final String name, final String description) {
		this.name = name;
		this.description = description;
	}
}