package bwfdm.sara.extractor.licensee;

import java.util.Arrays;
import java.util.List;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import bwfdm.sara.extractor.LazyFile;
import bwfdm.sara.extractor.LicenseExtractor;
import bwfdm.sara.extractor.LicenseFile;

public class LicenseeExtractor implements LicenseExtractor {
	private static LicenseeExtractor instance;

	private final ScriptingContainer container;
	private final Object extractor;

	private LicenseeExtractor() {
		container = new ScriptingContainer(LocalContextScope.CONCURRENT);
		container.setLoadPaths(Arrays.asList(
				"uri:classloader:/bwfdm/sara/extractor/licensee/ruby/lib",
				"uri:classloader:/bwfdm/sara/extractor/licensee"));
		container.runScriptlet("require 'license_extractor'");
		extractor = container.runScriptlet("LicenseExtractor");
	}

	static {
		new Thread("LicenseeExtractor background initialization") {
			@Override
			public void run() {
				// getInstance() does lazy-init so it doesn't slow down startup,
				// but that slows down the first license detection. thus we
				// initialize it here.
				LicenseeExtractor.getInstance();
			};
		}.start();
	}

	public static synchronized LicenseeExtractor getInstance() {
		if (instance == null)
			instance = new LicenseeExtractor();
		return instance;
	}

	@Override
	public LicenseFile detectLicense(final List<LazyFile> files) {
		return container.callMethod(extractor, "detect_license", files,
				LicenseFile.class);
	}
}
