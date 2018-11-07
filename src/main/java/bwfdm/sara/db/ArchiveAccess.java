package bwfdm.sara.db;

import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ArchiveAccess {
	PUBLIC("public"), PRIVATE("private");

	private final String displayName;

	private ArchiveAccess(final String name) {
		displayName = name;
	}

	@JsonCreator
	public static ArchiveAccess forDisplayName(final String name) {
		for (final ArchiveAccess a : values())
			if (a.displayName.equals(name))
				return a;
		throw new NoSuchElementException(name);
	}

	@JsonValue
	public String getDisplayName() {
		return displayName;
	}
}