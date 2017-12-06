package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.db.License;
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.project.Ref;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * primary the data class for
 * {@link Licenses#getLicenses(javax.servlet.http.HttpSession)}, but contains
 * some useful validation logic as well. this class consistently represents the
 * "keep autodetected license" state using the string "keep" (unlike the
 * database which consistently uses <code>null</code>), except for the
 * constructor (obviously). {@link #mapKeep(String)} and
 * {@link #unmapKeep(String)} convert between the two conventions.
 */
public class LicensesInfo {
	public static final String KEEP_LICENSE = "keep";
	public static final String MULTIPLE_LICENSES = "multi";
	public static final String UNRECOGNIZED_LICENSE = "other";
	private static final License UNRECOGNIZED_LICENSE_INFO = new License(
			UNRECOGNIZED_LICENSE, "Unrecognized License", null);

	/** list of all licenses supported by the server, in order of preference */
	@JsonProperty("supported")
	public final List<License> supported;
	/** holds both the autodetected and the user-set license for each branch */
	@JsonProperty("branches")
	public final List<LicenseInfo> branches;

	/**
	 * @param supported
	 *            the list of supported licenses, as returned by
	 *            {@link ConfigDatabase#getLicenses()}
	 * @param refs
	 *            the list of {@link Ref Refs} selected for archival
	 * @param detected
	 *            the autodetected licenses, as returned by
	 *            {@link MetadataExtractor#getLicenses()}
	 * @param user
	 *            the user-specified licenses, as stored in the database, ie.
	 *            {@link FrontendDatabase#getLicenses()}
	 */
	public LicensesInfo(final List<License> supported,
			final Collection<Ref> refs, final Map<Ref, LicenseFile> detected,
			final Map<Ref, String> user) {
		this.supported = supported;
		final Map<String, License> map = new HashMap<>();
		for (final License l : supported)
			map.put(l.id, l);
		map.put(null, UNRECOGNIZED_LICENSE_INFO);

		branches = new ArrayList<>();
		for (final Ref ref : refs) {
			final LicenseFile det = detected.get(ref);
			final String file;
			final License detectedLicense;
			if (det != null) {
				// if licenseID == null, this will use the null key added above
				// and set detectedLicense = UNRECOGNIZED_LICENSE
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
					// constraint enforcing only supported values in the
					// license field...
					throw new IllegalStateException("license " + license
							+ " for branch " + ref.path
							+ " not present in database!");
				userLicense = map.get(license).id;
			} else
				userLicense = KEEP_LICENSE;
			branches.add(new LicenseInfo(ref, file, detectedLicense,
					userLicense));
		}
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
	 * the ID of the single license selected by the user for all branches, or
	 * {@link Licenses#KEEP_LICENSE} if the user selected to keep the existing
	 * license for all branches, or <code>null</code> if the user's selection
	 * differs between branches.
	 */
	@JsonProperty("user")
	public final String getUserLicense() {
		if (hasMultipleUserLicenses())
			return null;

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
		return KEEP_LICENSE;
	}

	/**
	 * <code>false</code> if the combination of user selections and detected
	 * licenses ends up with all branches having the same license. in
	 * particular, this returns <code>true</code> if keeping the license for one
	 * branch yields a different license than another branch.
	 * 
	 * @see LicenseInfo#getEffectiveLicense()
	 */
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
	 * <code>true</code> if there is any branch that has neither an autodetected
	 * license nor a user-set one
	 */
	@JsonProperty("undefined")
	public boolean hasUndefinedLicenses() {
		for (final LicenseInfo b : branches)
			if (b.getEffectiveLicense() == null)
				return true;
		return false;
	}

	/** maps a <code>null</code> license to "keep" */
	public static String mapKeep(final String license) {
		return license == null ? KEEP_LICENSE : license;
	}

	/** maps a "keep" license to <code>null</code> */
	public static String unmapKeep(final String license) {
		return license.equals(KEEP_LICENSE) ? null : license;
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
}