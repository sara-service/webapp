package bwfdm.sara.git;

/** data class for tags returned from GitLab. */
public interface Tag {
	/** name of the tag. */
	public String getName();

	public boolean isProtected();
}