package bwfdm.sara.git;

public final class Branch {
	/** branch name. */
	public final String name;

	/**
	 * <code>true</code> if the branch is a protected branch (in GitLab), else
	 * <code>false</code>.
	 */
	public final boolean isProtected;

	/**
	 * <code>true</code> if the branch is the default branch (in GitLab), else
	 * <code>false</code>.
	 */
	public final boolean isDefault;

	public Branch(final String name, final boolean isProtected,
			final boolean isDefault) {
		this.name = name;
		this.isProtected = isProtected;
		this.isDefault = isDefault;
	}
}
