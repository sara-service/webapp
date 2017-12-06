package bwfdm.sara.extractor.licensee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import bwfdm.sara.extractor.LicenseExtractor;
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.transfer.RepoFile;
import bwfdm.sara.transfer.TransferRepo;

public class LicenseeExtractor implements LicenseExtractor {
	private static final Log logger = LogFactory
			.getLog(LicenseeExtractor.class);
	private static LicenseeExtractor instance;

	private final ScriptingContainer container;
	private final Object extractor;

	private LicenseeExtractor() {
		logger.info("Licensee initialization started");
		final long start = System.currentTimeMillis();

		container = new ScriptingContainer(LocalContextScope.CONCURRENT);
		container.setLoadPaths(Arrays.asList(
				"uri:classloader:/bwfdm/sara/extractor/licensee/ruby/lib",
				"uri:classloader:/bwfdm/sara/extractor/licensee"));
		container.runScriptlet("require 'license_extractor'");
		extractor = container.runScriptlet("LicenseExtractor");

		logger.info("Licensee initialization completed in "
				+ (System.currentTimeMillis() - start) + " ms");
	}

	public static synchronized LicenseeExtractor getInstance() {
		if (instance == null)
			instance = new LicenseeExtractor();
		return instance;
	}

	@Override
	public List<LicenseFile> detectLicenses(final TransferRepo repo,
			final List<RepoFile> files) {
		final List<LicenseFile> licenses = new ArrayList<>();
		for (final RepoFile file : files)
			licenses.add(detectLicense(repo, file));
		return licenses;
	}

	private LicenseFile detectLicense(final TransferRepo repo,
			final RepoFile file) {
		// TODO should probably cache by SHA-1 here!
		final LicenseeFile license = container.callMethod(extractor,
				"detect_license", new Object[] { repo, Arrays.asList(file) },
				LicenseeFile.class);
		if (license == null)
			return new LicenseFile(file, null, Float.NaN);
		return new LicenseFile(file, license.getID(), license.getScore());
	}
}
