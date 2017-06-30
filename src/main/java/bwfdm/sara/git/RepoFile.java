package bwfdm.sara.git;

public class RepoFile {
	private final String name;
	private final String hash;
	private final FileType type;

	public RepoFile(final String name, final String hash, final FileType type) {
		this.name = name;
		this.hash = hash;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getHash() {
		return hash;
	}

	public FileType getType() {
		return type;
	}

	public enum FileType {
		FILE, DIRECTORY
	}
}