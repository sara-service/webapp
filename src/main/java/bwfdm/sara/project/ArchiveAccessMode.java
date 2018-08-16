package bwfdm.sara.project;

import bwfdm.sara.publication.ItemType;

public enum ArchiveAccessMode {
	PRIVATE, PUBLIC;

	public ItemType getItemType() {
		switch (this) {
		case PRIVATE:
			return ItemType.ARCHIVE_HIDDEN;
		case PUBLIC:
			return ItemType.ARCHIVE_PUBLIC;
		default:
			throw new UnsupportedOperationException(
					"archive access mode " + this);
		}
	}

	public static ArchiveAccessMode forItemType(ItemType type) {
		switch (type) {
		case ARCHIVE_HIDDEN:
			return PRIVATE;
		case ARCHIVE_PUBLIC:
			return PUBLIC;
		default:
			throw new UnsupportedOperationException("item type " + type);
		}
	}
}
