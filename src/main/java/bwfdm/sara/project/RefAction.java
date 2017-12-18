package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.db.FrontendDatabase;

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

	/**
	 * Note: This is called indirectly by
	 * {@link FrontendDatabase#getRefActions()}. The {@link JsonProperty} field
	 * names correspond directly to database column names.
	 */
	@JsonCreator
	public RefAction(@JsonProperty("ref") final Ref ref,
			@JsonProperty("action") final PublicationMethod publicationMethod,
			@JsonProperty("start") final String firstCommit) {
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
