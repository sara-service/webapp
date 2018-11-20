package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Name {
	@JsonProperty("givenname")
	public final String givenname;
	@JsonProperty("surname")
	public final String surname;

	@JsonCreator
	public Name(@JsonProperty("surname") final String surname,
			@JsonProperty("givenname") final String givenname) {
		this.givenname = givenname;
		this.surname = surname;
	}

	@Override
	public boolean equals(Object obj) {
		final Name name = (Name) obj;
		return name.surname.equals(surname) && name.givenname.equals(givenname);
	}
}