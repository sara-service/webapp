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
	/** full name of the submitting user */
	SUBMITTER("submitter");

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
