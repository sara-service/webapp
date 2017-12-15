package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Tag;

public class Ref {
	/** ref in git syntax, ie. {@code heads/master} or {@code tags/foo} */
	@JsonProperty("path")
	public final String path;
	/** ref type (branch or tag). */
	@JsonProperty("type")
	public final RefType type;
	/**
	 * user-friendly name of ref, ie. {@code master} or {@code foo}. doesn't
	 * identify whether it's a branch or tag.
	 */
	@JsonProperty("name")
	public final String name;

	public Ref(final RefType type, final String name) {
		this.name = name;
		this.type = type;
		path = type.pathPrefix() + name;
	}

	public Ref(final Branch b) {
		this(RefType.BRANCH, b.name);
	}

	public Ref(final Tag t) {
		this(RefType.TAG, t.name);
	}

	@JsonCreator
	public Ref(final String path) {
		RefType type = null;
		for (final RefType t : RefType.values())
			if (path.startsWith(t.pathPrefix()))
				type = t;
		if (type == null)
			throw new IllegalArgumentException(
					"no RefType matches prefix of " + path);

		this.type = type;
		name = path.substring(type.pathPrefix().length());
		this.path = type.pathPrefix() + name;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		return path.equals(((Ref) obj).path);
	}

	@Override
	public String toString() {
		return "refs/" + path;
	}

	public enum RefType {
		@JsonProperty("branch")
		BRANCH, //
		@JsonProperty("tag")
		TAG;

		public String pathPrefix() {
			switch (this) {
			case TAG:
				return "tags/";
			case BRANCH:
				return "heads/";
			default:
				throw new IllegalArgumentException(name());
			}
		}
	}
}
