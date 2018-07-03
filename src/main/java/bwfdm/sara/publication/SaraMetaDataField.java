package bwfdm.sara.publication;

import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SaraMetaDataField {

	// author
	AUTHOR("sara-author"),
	// submitter
	SUBMITTER("sara-submitter"),
	// abstract (project description from git project)
	ABSTRACT("sara-abstract"),
	// date when git project has been archived
	DATE_ARCHIVED("sara-dateArchived"),
	// version information from git project
	VERSION("sara-version"),
	// title - name of the git project
	TITLE("sara-title"),
	// url the git project has been archived under
	ARCHIVE_URL("sara-archiveUrl"),
	// publisher info: sara service and its version information
	PUBLISHER("sara-publisher"),
	// item type for IRs, set this fix to e.g. "software"
	// TODO add a repo param for this as well
	TYPE("sara-type");

	private final String displayName;

	private SaraMetaDataField(final String name) {
		displayName = name;
	}

	@JsonCreator
	public static SaraMetaDataField forDisplayName(final String name) {
		for (final SaraMetaDataField f : SaraMetaDataField.values())
			if (f.displayName.equals(name))
				return f;
		throw new NoSuchElementException(name);
	}

	@JsonValue
	public String getDisplayName() {
		return displayName;
	}
}