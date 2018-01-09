package bwfdm.sara.db.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class LicenseMetadata {
	@JsonProperty("title")
	private String title;
	@JsonProperty("spdx-id")
	private String spdx;
	@JsonProperty("hidden")
	private boolean hidden;
	@JsonIgnore
	private String fullText;

	public String getTitle() {
		return title;
	}

	public String getSpdxID() {
		return spdx;
	}

	public boolean isHidden() {
		return hidden;
	}

	public String getFullText() {
		return fullText;
	}

	void setFullText(final String fullText) {
		this.fullText = fullText;
	}
}
