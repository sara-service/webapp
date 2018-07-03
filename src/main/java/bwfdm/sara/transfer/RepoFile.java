package bwfdm.sara.transfer;

import org.eclipse.jgit.lib.ObjectId;

public final class RepoFile {
	private final String name;
	private final ObjectId hash;
	private final FileType type;

	public RepoFile(final String name, final ObjectId hash,
			final FileType type) {
		this.name = name;
		this.hash = hash;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public ObjectId getHash() {
		return hash;
	}

	public FileType getType() {
		return type;
	}

	public enum FileType {
		FILE, DIRECTORY
	}

	@Override
	public String toString() {
		return hash + " " + type + " " + name;
	}
}
