package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.DataObject;
import bwfdm.sara.git.Tag;

/** data class for tags returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GHTag implements DataObject<Tag> {
	/** name of the tag. */
	@JsonProperty("name")
	String name;

	@Override
	public Tag toDataObject() {
		return new Tag(name, false);
	}
}