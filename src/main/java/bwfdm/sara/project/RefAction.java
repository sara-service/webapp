package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefAction {
	public static final String HEAD_COMMIT = "HEAD";

	/** selected archival option. */
	private final PublicationMethod publicationMethod;
	/** ID of first commit to archive. */
	private final String firstCommit;

	public RefAction(final PublicationMethod publicationMethod,
			final String firstCommit) {
		this.publicationMethod = publicationMethod;
		this.firstCommit = firstCommit;
	}

	@JsonProperty("publish")
	public PublicationMethod getPublicationMethod() {
		return publicationMethod;
	}

	@JsonProperty("firstCommit")
	public String getFirstCommit() {
		return firstCommit;
	}

	public enum PublicationMethod {
		PUBLISH_FULL, PUBLISH_ABBREV, PUBLISH_LATEST, ARCHIVE_PUBLIC, ARCHIVE_HIDDEN
	}

	@Override
	public String toString() {
		return publicationMethod + "@" + firstCommit;
	}
}
