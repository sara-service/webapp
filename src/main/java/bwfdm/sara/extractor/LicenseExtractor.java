package bwfdm.sara.extractor;

import java.util.List;

import bwfdm.sara.transfer.RepoFile;
import bwfdm.sara.transfer.TransferRepo;

public interface LicenseExtractor {
	public List<LicenseFile> detectLicenses(TransferRepo repo,
			List<RepoFile> files);
}
