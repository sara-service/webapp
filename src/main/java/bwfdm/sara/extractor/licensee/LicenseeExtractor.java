package bwfdm.sara.extractor.licensee;

import java.util.Arrays;
import java.util.List;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import bwfdm.sara.extractor.LazyFile;
import bwfdm.sara.extractor.LicenseExtractor;
import bwfdm.sara.extractor.LicenseFile;

public class LicenseeExtractor implements LicenseExtractor {
	private final ScriptingContainer container;
	private final Object extractor;

	public LicenseeExtractor() {
		container = new ScriptingContainer(LocalContextScope.CONCURRENT);
		container.setLoadPaths(Arrays.asList(
				"uri:classloader:/bwfdm/sara/extractor/licensee/ruby/lib",
				"uri:classloader:/bwfdm/sara/extractor/licensee"));
		container.runScriptlet("require 'license_extractor'");
		extractor = container.runScriptlet("LicenseExtractor");
	}

	@Override
	public LicenseFile detectLicense(final List<LazyFile> files) {
		return container.callMethod(extractor, "detect_license", files,
				LicenseFile.class);
	}
}
