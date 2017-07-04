package bwfdm.sara.project;

import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MetadataField {
	TITLE("title"), DESCRIPTION("description"), VERSION("version"), //
	LICENSE("license"), SOURCE_REF("source-ref");

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
