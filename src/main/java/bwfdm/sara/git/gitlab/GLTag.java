package bwfdm.sara.git.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for tags returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLTag {
	/** name of the tag. */
	@JsonProperty("name")
	String name;
}