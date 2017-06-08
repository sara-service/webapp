package bwfdm.sara.api;

import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for refs in the branch selection screen. */
@JsonInclude(Include.NON_NULL)
class Ref implements Comparable<Ref> {
	/**
	 * user-friendly name of ref, ie. {@code master} or {@code foo}. doesn't
	 * identify whether it's a branch or tag.
	 */
	@JsonProperty("name")
	final String name;
	/** ref in git syntax, ie. {@code heads/master} or {@code tags/foo} */
	@JsonProperty("ref")
	final String ref;
	/** ref type (branch or tag). */
	@JsonProperty("type")
	final RefType type;
	/** <code>true</code> if this is a protected branch */
	@JsonProperty("protected")
	final boolean isProtected;
	/** <code>true</code> if this is the default branch */
	@JsonProperty("default")
	final boolean isDefault;

	/** selected archival option. */
	@JsonProperty("action")
	Action action;
	/** ID of first commit to archive. */
	@JsonProperty("start")
	String start;

	Ref(final Branch b) {
		type = RefType.BRANCH;
		ref = "heads/" + b.name;
		name = b.name;
		isProtected = b.isProtected;
		isDefault = b.isDefault;
	}

	Ref(final Tag t) {
		type = RefType.TAG;
		ref = "tags/" + t.name;
		name = t.name;
		isProtected = t.isProtected;
		isDefault = false;
	}

	@Override
	public int compareTo(final Ref other) {
		// put default branch first so that it's the one selected by
		// default
		if (isDefault)
			return -1;
		if (other.isDefault)
			return +1;
		// branches before tags
		if (type == RefType.BRANCH && other.type != RefType.BRANCH)
			return -1;
		if (other.type == RefType.BRANCH && type != RefType.BRANCH)
			return +1;
		// protected branches are more likely to be the "main" branches,
		// so put them before unprotected (likely "side") branches
		if (isProtected && !other.isProtected)
			return -1;
		if (other.isProtected && !isProtected)
			return +1;
		// tiebreaker within those groups: lexicographic ordering
		return name.compareTo(other.name);
	}

	@Override
	public String toString() {
		return "Ref{" + ref + ", " + (isProtected ? "protected, " : "")
				+ "action=" + action + "}";
	}

	public enum Action {
		PUBLISH_FULL, PUBLISH_ABBREV, PUBLISH_LATEST, ARCHIVE_PUBLIC, ARCHIVE_HIDDEN
	}

	public enum RefType {
		BRANCH, TAG
	}
}
