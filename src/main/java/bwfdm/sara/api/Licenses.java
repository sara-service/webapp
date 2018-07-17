package bwfdm.sara.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.Config;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.db.License;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.project.LicensesInfo;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.Ref;

@RestController
@RequestMapping("/api/licenses")
public class Licenses {
	private static final String KEEP_LICENSE = "keep";

	@Autowired
	private Config config;

	@GetMapping("")
	public LicensesInfo getLicenses(final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();
		MetadataExtractor meta = project.getMetadataExtractor();
		return new LicensesInfo(getLicenseList(), db.getSelectedRefs(),
				meta.getLicenses(), meta.getLicenseSet(), db.getLicenses());
	}

	@PostMapping("")
	public void setAllLicenses(@RequestParam("license") final String license,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();

		final Map<Ref, String> licenses = new HashMap<>();
		for (final Ref ref : db.getSelectedRefs())
			licenses.put(ref, Licenses.unmapKeep(license));
		db.setLicenses(licenses);
		project.invalidateMetadata();
	}

	@PutMapping("")
	public void setLicense(@RequestBody final Map<Ref, String> licenses,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		for (final Entry<Ref, String> e : licenses.entrySet())
			e.setValue(Licenses.unmapKeep(e.getValue()));
		project.getFrontendDatabase().setLicenses(licenses);
		// db.setLicense(new Ref(refPath), LicensesInfo.unmapKeep(license));
		project.invalidateMetadata();
	}

	@GetMapping("supported")
	public List<License> getLicenseList() {
		return config.getConfigDatabase().getLicenses();
	}

	/**
	 * maps a "keep" license to <code>null</code>. necessary because the
	 * frontend cannot send <code>null</code> values. note that setting
	 * <code>null</code> values explicitly is still possible.
	 */
	private static String unmapKeep(final String license) {
		if (license == null)
			return null;
		if (license.equals(Licenses.KEEP_LICENSE))
			return null;
		return license;
	}
}
