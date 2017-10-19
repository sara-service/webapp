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
	@JsonProperty("default-branch")
	public final String master;

	public ProjectInfo(final String path, final String name,
			final String description, final String master) {
		this.path = path;
		this.name = name;
		this.description = description;
		this.master = master;
	}
}