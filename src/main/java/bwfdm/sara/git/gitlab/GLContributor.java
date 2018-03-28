package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.Contributor;
import bwfdm.sara.git.DataObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GLContributor implements DataObject<Contributor> {
	@JsonProperty("name")
	String name;
	@JsonProperty("email")
	String email;
	@JsonProperty("commits")
	int commits;

	@Override
	public Contributor toDataObject() {
		return new Contributor(name, email, commits);
	}
}
