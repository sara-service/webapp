package bwfdm.sara.publication.db;

import java.util.NoSuchElementException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PublicationField {
	/** the publication title */
	TITLE("title"),
	/** projects description / abstract */
	DESCRIPTION("description"),
	/** software version number */
	VERSION("version"), SUBMITTER("submitter"),
	/** ID of the institutional repository to publish the item in */
	PUBID("pubid"), // FIXME move into API, this needs to be read-only!!!
	PUBLICATION_REPOSITORY("pubrepo"),
	/** Whether the workflow will need to verify the users email address */
	VERIFY_USER("verify_user"),

	/**
	 * unique identifier of the selected collection in the institutional
	 * repository
	 */
	PUBREPO_COLLECTION("collection"), PUBREPO_REPOSITORYNAME(
			"pubrepo_displayname"), PUBREPO_COLLECTIONNAME(
					"collection_displayname"), ARCHIVE_URL(
							"archive_url"), REPOSITORY_URL("repository_url"),
	/**
	 * user's login email in the institutional repository. may well be different
	 * from the login email in the git repo!
	 */
	// this should be "email" so the browser will autocomplete the user's emails
	// for this field. it matches the field by name, not by type :(
	PUBREPO_LOGIN_EMAIL("email");

	private final String displayName;

	private PublicationField(final String name) {
		displayName = name;
	}

	@JsonCreator
	public static PublicationField forDisplayName(final String name) {
		for (final PublicationField f : PublicationField.values())
			if (f.displayName.equals(name))
				return f;
		throw new NoSuchElementException(name);
	}

	@JsonValue
	public String getDisplayName() {
		return displayName;
	}
}
