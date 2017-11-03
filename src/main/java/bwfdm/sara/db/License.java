package bwfdm.sara.db;

import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for licenses */
public class License {
	/** license ID as per https://spdx.org/licenses/ */
	@JsonProperty("id")
	public String id;
	/** license display name */
	@JsonProperty("name")
	public String displayName;
	/** URL to information about the license */
	@JsonProperty("url")
	public String infoURL;

	public License(final String id, final String displayName,
			final String infoURL) {
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
