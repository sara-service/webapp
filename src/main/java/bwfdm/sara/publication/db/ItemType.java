package bwfdm.sara.publication.db;

public enum ItemType { 
	archive, record, publication;
	private String str;
	static {
		archive.str = "archive";
		record.str = "record";
		publication.str = "publication";
	}
	public String toString() {
		return str;
	}
	static public ItemType fromString(String s) {
		s = s.toLowerCase();
		ItemType it = archive;
		if (s.equals(archive.str)) it = archive;
		if (s.equals(record.str)) it = record;
		if (s.equals(publication.str)) it = publication;
		return it;
	}
}