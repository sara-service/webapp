package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitHub email address list. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GHEmail {
	/** the actual email address */
	@JsonProperty("email")
	String address;
	/** <code>true</code> if this is the user's primary email address */
	@JsonProperty("primary")
	boolean isPrimary;
	/** <code>true</code> if the user's email address has been verified */
	@JsonProperty("verified")
	boolean isVerified;

	/**
	 * Used (and needed!) by Jackson to create an instance of the class when
	 * deserializing from JSON. Can be {@code private} because reflection
	 * doesn't care about visibility.
	 */
	private GHEmail() {
	}
}
