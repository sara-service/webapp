package bwfdm.sara.git.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for directory listings returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLRepoFile {
	/** file name. */
	@JsonProperty("name")
	String name;
	/** object type ({@code tree}, {@code blob}, etc) */
	@JsonProperty("type")
	String type;
	/** git object SHA1 */
	@JsonProperty("id")
	String hash;
}