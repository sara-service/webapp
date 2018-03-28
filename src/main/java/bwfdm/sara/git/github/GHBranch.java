package bwfdm.sara.git.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.Branch;

/** data class for branches returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GHBranch {
	/** branch name. */
	@JsonProperty("name")
	String name;
	/**
	 * <code>true</code> if the branch is a protected branch (in GitHub), else
	 * <code>false</code>.
	 */
	@JsonProperty("protected")
	boolean isProtected;

	Branch toBranch(final String master) {
		return new Branch(name, isProtected, name.equals(master));
	}
}