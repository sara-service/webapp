package bwfdm.sara.extractor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public interface LicenseFile {
	/** @return name of the file which contained the license */
	@JsonProperty("filename")
	public String getFile();

	/** @return License ID @see https://spdx.org/licenses/ */
	@JsonProperty("id")
	public String getID();

	/** @return license display name */
	@JsonProperty("name")
	public String getName();

	/** @return confidence score (0.0 … 1.0) */
	@JsonProperty("confidence")
	public float getScore();
}
