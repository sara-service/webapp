package bwfdm.sara.transfer;

import java.util.Map;

import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;

public interface MetadataSink {
	void setAutodetectedMetadata(Map<MetadataField, String> map);

	void setAutodetectedLicenses(Map<Ref, LicenseFile> licenses);
}
