package bwfdm.sara.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.db.License;
import bwfdm.sara.extractor.BranchLicense;
import bwfdm.sara.project.Project;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/licenses")
public class Licenses {
	@GetMapping("check")
	public RedirectView checkLicense(final HttpSession session) {
		// FIXME actually do some checking here!!
		// return new RedirectView("/license.html");
		return new RedirectView("/meta.html");
	}

	@GetMapping("")
	public LicenseDetectionResult getLicenses(final HttpSession session) {
		final Project project = Project.getInstance(session);
		return new LicenseDetectionResult(project.getMetadataExtractor()
				.getLicenses(), project.getFrontendDatabase().getLicenses());
	}

	@GetMapping("list")
	public List<License> getLicenseList(final HttpSession session) {
		return Project.getInstance(session).getFrontendDatabase().getLicenses();
	}

	static class LicenseDetectionResult {
		@JsonProperty("licenses")
		public final Set<License> licenses = new HashSet<>();
		@JsonProperty("branches")
		public final List<BranchLicense> branches;
		@JsonProperty("missing")
		public final boolean missing;

		LicenseDetectionResult(final List<BranchLicense> branches,
				final List<License> allLicenses) {
			this.branches = branches;

			final Map<String, License> temp = new HashMap<>();
			for (final License l : allLicenses)
				temp.put(l.id, l);
			boolean miss = false;
			for (final BranchLicense b : branches)
				if (b.licenseID != null)
					licenses.add(temp.get(b.licenseID));
				else
					miss = true;
			missing = miss;
		}
	}
}
