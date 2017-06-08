package bwfdm.sara.git;

/** data class for tags returned from GitLab. */
public final class Tag {
	/** name of the tag. */
	public final String name;

	public final boolean isProtected;

	public Tag(final String name, final boolean isProtected) {
		this.name = name;
		this.isProtected = isProtected;
	}
}