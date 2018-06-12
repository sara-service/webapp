package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for GitHub user info. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GHUserInfo {
	/** internal user ID (unchangeable?) */
	@JsonProperty("id")
	String userID;
	/**
	 * user's display name (<code>null</code> unless the user entered that
	 * somewhere)
	 */
	@JsonProperty("name")
	String displayName;
	/** user's login name, shown on the site when no display name entered */
	@JsonProperty("login")
	String loginName;

	/**
	 * Used (and needed!) by Jackson to create an instance of the class when
	 * deserializing from JSON. Can be {@code private} because reflection
	 * doesn't care about visibility.
	 */
	private GHUserInfo() {
	}

	public String getEffectiveDisplayName() {
		if (displayName != null && !displayName.isEmpty())
			return displayName;
		return loginName;
	}
}
