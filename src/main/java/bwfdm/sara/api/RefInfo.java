package bwfdm.sara.api;

import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Tag;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.Ref.RefType;
import bwfdm.sara.project.RefAction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * data class for refs in the branch selection screen. used by
 * {@link Repository#getBranches(javax.servlet.http.HttpSession)} mostly.
 */
@JsonInclude(Include.NON_NULL)
class RefInfo implements Comparable<RefInfo> {
	/** {@link Ref} data class */
	@JsonProperty("ref")
	final Ref ref;
	/** <code>true</code> if this is a protected branch */
	@JsonProperty("protected")
	final boolean isProtected;
	/** <code>true</code> if this is the default branch */
	@JsonProperty("default")
	final boolean isDefault;
	@JsonProperty("action")
	RefAction action;

	RefInfo(final Branch b) {
		ref = new Ref(b);
		isProtected = b.isProtected;
		isDefault = b.isDefault;
	}

	RefInfo(final Tag t) {
		ref = new Ref(t);
		isProtected = t.isProtected;
		isDefault = false;
	}

	@Override
	public int compareTo(final RefInfo other) {
		// put default branch first so that it's the one selected by
		// default
		if (isDefault)
			return -1;
		if (other.isDefault)
			return +1;
		// branches before tags
		if (ref.type == RefType.BRANCH && other.ref.type != RefType.BRANCH)
			return -1;
		if (other.ref.type == RefType.BRANCH && ref.type != RefType.BRANCH)
			return +1;
		// protected branches are more likely to be the "main" branches,
		// so put them before unprotected (likely "side") branches
		if (isProtected && !other.isProtected)
			return -1;
		if (other.isProtected && !isProtected)
			return +1;
		// tiebreaker within those groups: lexicographic ordering
		return ref.name.compareTo(other.ref.name);
	}

	@Override
	public String toString() {
		return "RefInfo{" + ref + ", " + (isProtected ? "protected, " : "")
				+ "action=" + action + "}";
	}
}
