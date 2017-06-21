package bwfdm.sara.extractor;

import java.util.List;

public interface LicenseExtractor {
	public LicenseFile detectLicense(final List<LazyFile> files);
}
