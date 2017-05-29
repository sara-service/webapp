package bwfdm.sara.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for tags returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {
	/** name of the tag. */
	@JsonProperty("name")
	public String name;
}