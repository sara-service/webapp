package bwfdm.sara.git;

public interface Branch {
	/** branch name. */
	public String getName();

	/**
	 * <code>true</code> if the branch is a protected branch (in GitLab), else
	 * <code>false</code>.
	 */
	public boolean isProtected();

	/**
	 * <code>true</code> if the branch is the default branch (in GitLab), else
	 * <code>false</code>.
	 */
	public boolean isDefault();
}
