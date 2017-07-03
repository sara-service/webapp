package bwfdm.sara.git.gitlab;

import bwfdm.sara.git.RepoFile;
import bwfdm.sara.git.RepoFile.FileType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for directory listings returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLRepoFile implements GLDataObject<RepoFile> {
	/** file name. */
	@JsonProperty("name")
	String name;
	/** object type ({@code tree}, {@code blob}, etc) */
	@JsonProperty("type")
	String type;
	/** git object SHA1 */
	@JsonProperty("id")
	String hash;

	@Override
	public RepoFile toDataObject() {
		FileType t;
		if (type.equals("tree"))
			t = FileType.DIRECTORY;
		else if (type.equals("blob"))
			t = FileType.FILE;
		else
			t = FileType.OTHER;
		return new RepoFile(name, hash, t);
	}
}