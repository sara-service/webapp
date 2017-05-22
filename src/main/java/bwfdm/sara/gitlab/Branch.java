package bwfdm.sara.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Branch {
	/** branch name. */
	@JsonProperty("name")
	public String name;
	/**
	 * <code>true</code> if the branch is a protected branch (in GitLab), else
	 * <code>false</code>.
	 */
	@JsonProperty("protected")
	public boolean prot;
}