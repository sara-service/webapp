package bwfdm.sara.transfer;

import java.util.List;
import java.util.Map;

import bwfdm.sara.extractor.BranchLicense;
import bwfdm.sara.project.MetadataField;

public interface MetadataSink {
	void setAutodetectedMetadata(Map<MetadataField, String> map);

	void setAutodetectedLicenses(List<BranchLicense> licenses);
}
