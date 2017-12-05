package bwfdm.sara.publication.db;

public enum ItemStatus { 
	created, deprecated, verified, submitted, accepted, rejected, processed;
	private String str;
	static {
		created.str = "created";
		deprecated.str = "deprecated";
		verified.str = "verified";
		submitted.str = "submitted";
		accepted.str = "accepted";
		rejected.str = "rejected";
		processed.str = "processed";
	}
	public String toString() {
		return str;
	}
	static public ItemStatus fromString(String s) {
		s = s.toLowerCase();
		ItemStatus is = deprecated;
		if (s.equals(created.str)) is = created;
		if (s.equals(deprecated.str)) is = deprecated;
		if (s.equals(verified.str)) is = verified;
		if (s.equals(submitted.str)) is = submitted;
		if (s.equals(accepted.str)) is = accepted;
		if (s.equals(rejected.str)) is = rejected;
		if (s.equals(processed.str)) is = processed;
		return is;
	}
}
