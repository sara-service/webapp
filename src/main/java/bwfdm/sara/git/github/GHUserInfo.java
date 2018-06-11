package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.AuthProvider.UserInfo;
import bwfdm.sara.git.DataObject;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GHUserInfo implements DataObject<UserInfo> {
	/** internal user ID (unchangeable?) */
	@JsonProperty("id")
	String userID;
	/** user's primary email address (verified and guranteed unique) */
	@JsonProperty("email")
	String email;
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

	@Override
	public UserInfo toDataObject() {
		return new UserInfo(userID, email, getEffectiveDisplayName());
	}
}
