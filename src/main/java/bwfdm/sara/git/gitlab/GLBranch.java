package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.Branch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for branches returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLBranch implements Branch {
	/** branch name. */
	@JsonProperty("name")
	String name;
	/**
	 * <code>true</code> if the branch is a protected branch (in GitLab), else
	 * <code>false</code>.
	 */
	@JsonProperty("protected")
	private boolean isProtected;
	/**
	 * <code>true</code> if the branch is the default branch (in GitLab), else
	 * <code>false</code>.
	 */
	boolean isDefault;

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public boolean isProtected() {
		return isProtected;
	}

	@Override
	public String getName() {
		return name;
	}
}