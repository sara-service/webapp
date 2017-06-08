package bwfdm.sara.git.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for branches returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLBranch {
	/** branch name. */
	@JsonProperty("name")
	String name;
	/**
	 * <code>true</code> if the branch is a protected branch (in GitLab), else
	 * <code>false</code>.
	 */
	@JsonProperty("protected")
	boolean isProtected;
	/**
	 * <code>true</code> if the branch is the default branch (in GitLab), else
	 * <code>false</code>.
	 */
	boolean isDefault;
}