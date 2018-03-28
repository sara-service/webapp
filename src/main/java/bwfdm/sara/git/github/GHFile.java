package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for files downloaded from GitLab using the API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GHFile {
	/** object type, to distinguish files from directories etc. */
	@JsonProperty("type")
	String type;
	/** content encoding, to validate it's really base64 */
	@JsonProperty("encoding")
	String encoding;
	/** filename (without path) */
	@JsonProperty("name")
	String name;
	/** file contents, base64-encoded. */
	@JsonProperty("content")
	String data;
	/** SHA-1 of contents, for atomic replace. */
	@JsonProperty("sha")
	String sha1;
}
