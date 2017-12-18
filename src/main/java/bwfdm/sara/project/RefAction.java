package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefAction {
	public static final String HEAD_COMMIT = "HEAD";

	@JsonProperty("ref")
	public final Ref ref;
	/** selected archival option. */
	@JsonProperty("publish")
	public final PublicationMethod publicationMethod;
	/** ID of first commit to archive. either the SHA-1 or {@link #HEAD_COMMIT} */
	@JsonProperty("firstCommit")
	public final String firstCommit;

	public RefAction(final Ref ref, final PublicationMethod publicationMethod,
			final String firstCommit) {
		this.ref = ref;
		this.publicationMethod = publicationMethod;
		this.firstCommit = firstCommit;
	}

	public enum PublicationMethod {
		PUBLISH_FULL, PUBLISH_ABBREV, PUBLISH_LATEST, ARCHIVE_PUBLIC, ARCHIVE_HIDDEN
	}

	@Override
	public String toString() {
		return publicationMethod + "@" + firstCommit;
	}
}
