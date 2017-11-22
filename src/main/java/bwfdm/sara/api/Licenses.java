package bwfdm.sara.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.Config;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.db.License;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.Ref;

@RestController
@RequestMapping("/api/licenses")
public class Licenses {
	@Autowired
	private Config config;

	@GetMapping("")
	public LicensesInfo getLicenses(final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();
		return new LicensesInfo(getLicenseList(), db.getRefActions().keySet(),
				project.getMetadataExtractor().getLicenses(), db.getLicenses());
	}

	@PostMapping("all")
	public void setAllLicenses(@RequestParam("license") final String license,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();

		final Map<Ref, String> licenses = new HashMap<>();
		for (final Ref ref : db.getRefActions().keySet())
			licenses.put(ref, LicensesInfo.unmapKeep(license));
		db.setLicenses(licenses);
		project.invalidateMetadata();
	}

	@PostMapping("{ref}")
	public void setLicense(@PathVariable("ref") final String refPath,
			@RequestParam("license") final String license,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();
		db.setLicense(Ref.fromPath(refPath), LicensesInfo.unmapKeep(license));
		project.invalidateMetadata();
	}

	@GetMapping("supported")
	public List<License> getLicenseList() {
		return config.getConfigDatabase().getLicenses();
	}
}
