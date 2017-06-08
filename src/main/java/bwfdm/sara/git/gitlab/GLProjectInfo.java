package bwfdm.sara.git.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
class GLProjectInfo {
	/** default branch, <code>master</code> by default. */
	@JsonProperty("default_branch")
	String master;
	@JsonProperty("name")
	String name;
	@JsonProperty("description")
	String description;

	/**
	 * Used (and needed!) by Jackson to create an instance of the class when
	 * deserializing frpm JSON. Can be {@code private} because reflection
	 * doesn't care about visibility.
	 */
	@SuppressWarnings("unused")
	private GLProjectInfo() {
	}

	GLProjectInfo(final String name, final String description) {
		this.name = name;
		this.description = description;
	}
}