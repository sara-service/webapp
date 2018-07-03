package bwfdm.sara.extractor;

import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.transfer.RepoFile;

@JsonInclude(Include.NON_NULL)
public class LicenseFile {
	/** name of the file which contains the license, never <code>null</code> */
	@JsonProperty("filename")
	public final String path;
	/** SHA-1 of the file which contains the license, never <code>null</code> */
	@JsonIgnore
	public final ObjectId hash;
	/**
	 * {@link https://spdx.org/licenses/ SPDX License ID}, or <code>null</code>
	 * if there is a license file but its contents are unrecognizable
	 */
	@JsonProperty("id")
	public final String licenseID;
	/** confidence score (0.0 … 1.0) */
	@JsonProperty("confidence")
	public final float score;

	public LicenseFile(final String path, final ObjectId hash,
			final String licenseID, final float score) {
		this.path = path;
		this.hash = hash;
		this.licenseID = licenseID;
		this.score = score;
	}

	public LicenseFile(final RepoFile file, final String licenseID,
			final float score) {
		path = file.getName();
		hash = file.getHash();
		this.licenseID = licenseID;
		this.score = score;
	}

	@Override
	public boolean equals(Object obj) {
		return ((LicenseFile) obj).hash.equals(hash);
	}

	@Override
	public int hashCode() {
		return hash.hashCode();
	}
}
