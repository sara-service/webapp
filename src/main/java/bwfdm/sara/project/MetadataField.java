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
	/**
	 * branch the version number was read from. this isn't really a metadatum,
	 * but it should be remembered for the user's next visit anyway.
	 */
	VERSION_BRANCH("versionbranch"),
	/** ID of the institutional repository to publish the item in */
	PUBLICATION_REPOSITORY("pubrepo"),
	/**
	 * unique identifier of the selected collection in the institutional
	 * repository
	 */
	PUBREPO_COLLECTION("collection"),
	/** user's login email in the institutional repository */
	PUBREPO_LOGIN_EMAIL("email");

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
