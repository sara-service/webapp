package bwfdm.sara.project;

import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MetadataField {
	/** the publication title */
	TITLE("title"),
	/** projects description / abstract */
	DESCRIPTION("description"),
	/** software version number */
	VERSION("version"),
	/** primary (default) branch in repo. */
	MAIN_BRANCH("master"),
	/** surname (lastname, for Europeans) of the submitting user */
	SUBMITTER_SURNAME("submitter_surname"),
	/** given name (firstname, for Europeans) of the submitting user */
	SUBMITTER_GIVENNAME("submitter_givenname");

	private final String displayName;

	private MetadataField(final String name) {
		displayName = name;
	}

	@JsonCreator
	public static MetadataField forDisplayName(final String name) {
		for (final MetadataField f : MetadataField.values())
			if (f.displayName.equals(name))
				return f;
		throw new NoSuchElementException(name);
	}

	@JsonValue
	public String getDisplayName() {
		return displayName;
	}
}
