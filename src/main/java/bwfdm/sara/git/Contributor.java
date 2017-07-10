package bwfdm.sara.git;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contributor implements Comparable<Contributor> {
	@JsonProperty("name")
	public final String name;
	@JsonProperty("email")
	public final String email;
	@JsonProperty("commits")
	public final int commits;

	public Contributor(final String name, final String email, final int commits) {
		this.name = name;
		this.email = email;
		this.commits = commits;
	}

	@Override
	public int compareTo(final Contributor o) {
		return o.commits - commits;
	}
}
