package bwfdm.sara.extractor;

import bwfdm.sara.project.Ref;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BranchLicense {
	/** {@link Ref} to which this license applies */
	@JsonProperty("ref")
	public final Ref ref;
	/** name of the file containing the license */
	@JsonProperty("file")
	public final String filename;
	/** ID of the license that was detected */
	@JsonProperty("license")
	public final String licenseID;

	public BranchLicense(final String refPath, final String filename,
			final String license) {
		ref = Ref.fromPath(refPath);
		this.filename = filename;
		this.licenseID = license;
	}

	public BranchLicense(final String refPath, final LicenseFile lic) {
		ref = Ref.fromPath(refPath);
		filename = lic.getFile();
		licenseID = lic.getID();
	}

	@Override
	public String toString() {
		return "BranchLicense{" + ref + "#" + filename + ": " + licenseID + "}";
	}
}
