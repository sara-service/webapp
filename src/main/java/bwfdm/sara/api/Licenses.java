package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.Ref;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/licenses")
public class Licenses {
	public static final String KEEP_LICENSE = "keep";
	public static final String MULTIPLE_LICENSES = "multi";
	public static final String UNRECOGNIZED_LICENSE = "other";
	private static final License UNRECOGNIZED_LICENSE_INFO = new License(
			UNRECOGNIZED_LICENSE, "Unrecognized License", null);

	@Autowired
	private Config config;

	@GetMapping("")
	public LicensesInfo getLicenses(final HttpSession session) {
		final List<License> supported = getLicenseList();
		final Map<String, License> map = new HashMap<>();
		for (final License l : supported)
			map.put(l.id, l);
		map.put(null, UNRECOGNIZED_LICENSE_INFO);

		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();
		final Map<Ref, LicenseFile> detected = project.getMetadataExtractor()
				.getLicenses();
		final Map<Ref, String> user = db.getLicenses();

		final List<LicenseInfo> licenses = new ArrayList<>();
		for (final Ref ref : db.getRefActions().keySet()) {
			final LicenseFile det = detected.get(ref);
			final String file;
			final License detectedLicense;
			if (det != null) {
				// if det.getID() == null, this will use the null key added
				// above and set detectedLicense = UNRECOGNIZED_LICENSE
				detectedLicense = map.get(det.licenseID);
				file = det.path;
			} else {
				detectedLicense = null;
				file = null;
			}

			final String license = user.get(ref);
			final String userLicense;
			if (license != null) {
				if (!map.containsKey(license))
					// this really shouldn't happen. there's a foreign key
					// constraint enforcing only supported values in the license
					// field...
					throw new IllegalStateException("license " + license
							+ " for branch " + ref.path
							+ " not present in database!");
				userLicense = map.get(license).id;
			} else
				userLicense = KEEP_LICENSE;
			licenses.add(new LicenseInfo(ref, file, detectedLicense,
					userLicense));
		}
		return new LicensesInfo(supported, map, licenses);
	}

	public static class LicensesInfo {
		/** list of all licenses supported by the server, in order of preference */
		@JsonProperty("supported")
		public final List<License> supported;
		/** holds both the autodetected and the user-set license for each branch */
		@JsonProperty("branches")
		public final List<LicenseInfo> branches;

		private LicensesInfo(final List<License> supported,
				final Map<String, License> map, final List<LicenseInfo> branches) {
			this.supported = supported;
			this.branches = branches;
		}

		/** set of all licenses autodetected in the project */
		@JsonProperty("detected")
		public final Set<License> getDetectedLicenses() {
			final HashSet<License> res = new HashSet<>();
			for (final LicenseInfo b : branches)
				if (b.detected != null)
					res.add(b.detected);
			return res;
		}

		/**
		 * the ID of the single license selected by the user for all branches,
		 * or <code>null</code> if the user selected to keep the existing
		 * license for all branches, or {@link Licenses#MULTIPLE_LICENSES} if
		 * the user's selection differs between branches.
		 */
		@JsonProperty("user")
		public final String getUserLicense() {
			if (hasMultipleUserLicenses())
				return MULTIPLE_LICENSES;

			// possible situations at this point:
			// - user selected the same license everywhere: we obviously want to
			//   return that single license.
			// - user selected "keep" for some, but consistently overrides other
			//   branches: we need to keep the override to make sure those
			//   branches are still overridden. (note that we only get here if
			//   keeping yields the same license as overriding!)
			// - user selected "keep" everywhere: we obviously want to "keep".
			// since at this point we know there is at most one license in the
			// user's selections, this is quite trivial to implement:
			for (final LicenseInfo b : branches)
				if (b.user != null)
					return b.user;
			return null;
		}

		@JsonProperty("multiple")
		public boolean hasMultipleUserLicenses() {
			String license = null;
			for (final LicenseInfo b : branches) {
				final String lic = b.getEffectiveLicense();
				if (lic == null)
					continue;

				if (license == null)
					license = lic;
				else if (!license.equals(lic))
					return true;
			}
			return false;
		}

		/**
		 * <code>true</code> if there is any branch that doesn't have an
		 * autodetected license file
		 */
		@JsonProperty("missing")
		public boolean hasMissingLicenses() {
			for (final LicenseInfo b : branches)
				if (b.detected == null)
					return true;
			return false;
		}

		/**
		 * <code>true</code> if there is any branch that has neither an
		 * autodetected license nor a user-set one
		 */
		@JsonProperty("undefined")
		public boolean hasUndefinedLicenses() {
			for (final LicenseInfo b : branches)
				if (b.getEffectiveLicense() == null)
					return true;
			return false;
		}
	}

	public static class LicenseInfo {
		/** {@link Ref} to which this license applies */
		@JsonProperty("ref")
		public final Ref ref;
		/**
		 * name of the file containing the license; <code>null</code> if file
		 * missing
		 */
		@JsonProperty("file")
		public final String file;
		/**
		 * {@link License} that was autodetected; <code>null</code> if file
		 * missing, or if the file contents aren't recognizable as one of the
		 * supported licenses
		 */
		@JsonProperty("detected")
		public final License detected;
		/**
		 * ID of the license selected by the user for this branch;
		 * <code>null</code> if the user selected no license or selected to keep
		 * the current license (no need to distinguish because in both cases we
		 * just want to keep the existing license)
		 */
		@JsonProperty("user")
		public final String user;

		public LicenseInfo(final Ref ref, final String file,
				final License detected, final String user) {
			this.ref = ref;
			this.file = file;
			this.detected = detected;
			this.user = user;
		}

		/**
		 * returns the ID of the license that was effectively selected. this is
		 * the user's choice if there is one, or the detected license if the
		 * user chose to keep the detected license (possibly
		 * {@link Licenses#UNRECOGNIZED_LICENSE} if the license file is present
		 * but cannot be recognized as one of the supported licenses), or
		 * <code>null</code> if the user made no choice and no license was
		 * detected.
		 */
		@JsonProperty("effective")
		public String getEffectiveLicense() {
			if (user != null)
				return user;
			if (detected != null)
				return detected.id;
			return null;
		}
	}

	@PostMapping("all")
	public void setAllLicenses(@RequestParam("license") final String license,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();

		final String licenseID = license.equals(KEEP_LICENSE) ? null : license;
		final Map<Ref, String> licenses = new HashMap<>();
		for (final Ref ref : db.getRefActions().keySet())
			licenses.put(ref, licenseID);
		db.setLicenses(licenses);
		project.invalidateMetadata();
	}

	@PostMapping("{ref}")
	public void setLicense(@PathVariable("ref") final String refPath,
			@RequestParam("license") final String license,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();
		final String licenseID = license.equals(KEEP_LICENSE) ? null : license;
		db.setLicense(Ref.fromPath(refPath), licenseID);
		project.invalidateMetadata();
	}

	@GetMapping("supported")
	public List<License> getLicenseList() {
		return config.getConfigDatabase().getLicenses();
	}
}
