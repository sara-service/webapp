package bwfdm.sara.git.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.AuthProvider.UserInfo;
import bwfdm.sara.git.DataObject;

/** data class for GitLab project info. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLUserInfo implements DataObject<UserInfo> {
	/** internal user ID (unchangeable) */
	@JsonProperty("id")
	String userID;
	/** user's primary email address (verified and guranteed unique) */
	@JsonProperty("email")
	String email;
	/** user's display name */
	@JsonProperty("name")
	String displayName;

	/**
	 * Used (and needed!) by Jackson to create an instance of the class when
	 * deserializing from JSON. Can be {@code private} because reflection
	 * doesn't care about visibility.
	 */
	private GLUserInfo() {
	}

	@Override
	public UserInfo toDataObject() {
		return new UserInfo(userID, email, displayName);
	}
}
