package bwfdm.sara.project;

import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Tag;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	public static Ref fromPath(final String path) {
		for (final RefType type : RefType.values())
			if (path.startsWith(type.pathPrefix()))
				return new Ref(type, path.substring(type.pathPrefix().length()));
		throw new IllegalArgumentException("no RefType matches prefix of "
				+ path);
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
