package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.Tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for tags returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLTag implements Tag {
	/** name of the tag. */
	@JsonProperty("name")
	private String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isProtected() {
		// branches CAN be protected, but the API doesn't return that field
		return false;
	}
}