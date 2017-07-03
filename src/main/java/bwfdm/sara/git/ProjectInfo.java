package bwfdm.sara.git;

import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitLab project info. */
public final class ProjectInfo {
	@JsonProperty("path")
	public final String path;
	@JsonProperty("name")
	public final String name;
	@JsonProperty("description")
	public final String description;

	public ProjectInfo(final String path, final String name,
			final String description) {
		this.path = path;
		this.name = name;
		this.description = description;
	}
}