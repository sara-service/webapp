package bwfdm.sara.transfer;

import java.util.Map;

import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.project.Ref;

public interface MetadataSink {
	void setAutodetectedLicenses(Map<Ref, LicenseFile> licenses);
}
