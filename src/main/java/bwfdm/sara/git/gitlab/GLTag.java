package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.Tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for tags returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLTag implements GLDataObject<Tag> {
	/** name of the tag. */
	@JsonProperty("name")
	String name;

	@Override
	public Tag toDataObject() {
		// tags CAN be protected, but the GitLab API doesn't return that
		// field...
		return new Tag(name, false);
	}
}