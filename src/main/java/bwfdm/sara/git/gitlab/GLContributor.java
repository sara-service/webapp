package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.Contributor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GLContributor implements GLDataObject<Contributor> {
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
