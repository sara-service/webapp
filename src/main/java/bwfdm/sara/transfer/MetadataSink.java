package bwfdm.sara.transfer;

import java.util.Map;

import bwfdm.sara.project.MetadataField;

public interface MetadataSink {
	void setAutodetectedMetadata(Map<MetadataField, String> map);
}
