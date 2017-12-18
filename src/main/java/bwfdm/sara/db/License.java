package bwfdm.sara.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for licenses */
public class License {
	/** license ID as per https://spdx.org/licenses/ */
	@JsonProperty("id")
	public final String id;
	/** license display name */
	@JsonProperty("name")
	public final String displayName;
	/** URL to information about the license */
	@JsonProperty("url")
	public final String infoURL;

	/**
	 * Note: This is called indirectly by {@link ConfigDatabase#getLicenses()}.
	 * The {@link JsonProperty} field names correspond directly to database
	 * column names.
	 */
	@JsonCreator
	public License(@JsonProperty("id") final String id,
			@JsonProperty("display_name") final String displayName,
			@JsonProperty("info_url") final String infoURL) {
		this.id = id;
		this.displayName = displayName;
		this.infoURL = infoURL;
	}

	@Override
	public boolean equals(final Object obj) {
		return id.equals(((License) obj).id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "LicenseInfo{" + id + ": " + displayName + " url=" + infoURL
				+ "}";
	}
}
