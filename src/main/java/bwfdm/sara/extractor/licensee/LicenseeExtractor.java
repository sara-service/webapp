package bwfdm.sara.extractor.licensee;

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
	public LicenseFile detectLicense(final TransferRepo repo,
			final List<RepoFile> files) {
		final LicenseeFile license = container.callMethod(extractor,
				"detect_license", new Object[] { repo, files },
				LicenseeFile.class);
		if (license == null)
			return null;

		// unfortunately Licensee only returns the filename, so we need to
		// manually find the corresponding file hash now...
		for (final RepoFile file : files)
			if (file.getName().equals(license.getFile()))
				return new LicenseFile(file, license.getID(),
						license.getScore());
		throw new IllegalStateException(
				"licensee detected nonexistent license file "
						+ license.getFile());
	}
}
