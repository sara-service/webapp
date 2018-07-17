package bwfdm.sara.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.api.Licenses;
import bwfdm.sara.db.ConfigDatabase;
import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.db.License;
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.MetadataExtractor;

/**
 * primary the data class for
 * {@link Licenses#getLicenses(javax.servlet.http.HttpSession)}, but contains
 * some useful validation logic as well. this class consistently represents the
 * "keep autodetected license" state using the string "keep" (unlike the
 * database which consistently uses <code>null</code>), except for the
 * constructor (obviously). {@link #mapKeep(String)} and
 * {@link Licenses#unmapKeep(String)} convert between the two conventions.
 */
public class LicensesInfo {
	public static final String MULTIPLE_LICENSES = "multi";
	public static final String UNRECOGNIZED_LICENSE = "other";
	private static final License UNRECOGNIZED_LICENSE_INFO = new License(
			UNRECOGNIZED_LICENSE, "Some Other License", null);

	/** list of all licenses supported by the server, in order of preference */
	@JsonProperty("supported")
	public final List<License> supportedList;
	/** holds both the autodetected and the user-set license for each branch */
	@JsonProperty("branches")
	public final List<LicenseInfo> branches;
	@JsonProperty("detected")
	public final Set<License> detectedLicenses;
	@JsonIgnore
	private final Map<String, License> supportedMap;
	@JsonIgnore
	private Set<LicenseFile> licensesSet;

	/**
	 * @param supported
	 *            the list of supported licenses, as returned by
	 *            {@link ConfigDatabase#getLicenses()} (ie. sorted by
	 *            preference)
	 * @param refs
	 *            the list of {@link Ref Refs} selected for archival, as
	 *            returned by {@link FrontendDatabase#getSelectedRefs()}
	 * @param detected
	 *            the autodetected licenses, as returned by
	 *            {@link MetadataExtractor#getLicenses()}
	 * @param licensesSet
	 *            the set of all detected unique license files, as returned by
	 *            {@link MetadataExtractor#getLicenseSet()}
	 * @param user
	 *            the user-specified licenses, as stored in the database, ie.
	 *            {@link FrontendDatabase#getLicenses()}
	 */
	public LicensesInfo(final List<License> supported,
			final Collection<Ref> refs, final Map<Ref, LicenseFile> detected,
			final Set<LicenseFile> licensesSet,
			final Map<Ref, String> user) {
		this.supportedList = supported;
		this.licensesSet = licensesSet;
		// auxiliary map for mapping licenses by ID
		supportedMap = new HashMap<>();
		for (final License l : supported)
			supportedMap.put(l.id, l);
		supportedMap.put(null, UNRECOGNIZED_LICENSE_INFO);

		// build list of distinct detected licenses. there must be exactly one
		// distinct license for "keeping" to be meaningful.
		// note that this can contain duplicates if there are several copies of
		// the same license, but with different file hashes!
		this.detectedLicenses = new HashSet<>();
		for (final LicenseFile det : licensesSet)
			detectedLicenses.add(supportedMap.get(det.licenseID));

		// build per-branch license list
		branches = new ArrayList<>();
		for (final Ref ref : refs)
			branches.add(
					new LicenseInfo(ref, detected.get(ref), user.get(ref)));
	}

	/**
	 * the ID of the single license selected by the user for all branches, or
	 * {@link Licenses#KEEP_LICENSE} if the user selected to keep the existing
	 * license for all branches, or <code>null</code> if the user's selection
	 * differs between branches.
	 */
	@JsonProperty("user")
	public final String getUserLicense() {
		if (!hasConsistentLicenses())
			return MULTIPLE_LICENSES;

		// possible situations at this point:
		// - user selected the same license everywhere: we obviously want to
		//   return that single license.
		// - user selected "keep" everywhere: we obviously want to "keep".
		// - no linceses present: "keep" so the user has to choose something
		// since at this point we know there is at most one license in the
		// user's selections, this is quite trivial to implement:
		for (final LicenseInfo b : branches)
			if (b.user != null)
				return b.user;
		return null;
	}

	/**
	 * <code>true</code> if the combination of user selections and detected
	 * licenses ends up with all branches having the same license file. this is
	 * the case if
	 * <ul>
	 * <li>there is a single replacement license that applies to all branches
	 * (ie. no branch is set to a different replacement license, and no branch
	 * is set to keep its license)
	 * <li>there is a single license file that applies to all branches (ie. no
	 * branch is set to a replacement license, and there is only a single
	 * license file to choose from)
	 * <li>no branch has a license (in that case, whatever the user picks will
	 * inherently be consistent)
	 * <li>there is a single effective license and every branch contains an
	 * appropriate form of it (ie. all branches have a license file which is
	 * detected as the same license, eg. several GPL v3's with different
	 * placeholders). we couldn't choose a license file for branches without a
	 * license here, but as long as there are no branches without license that's
	 * perfectly fine.
	 * </ul>
	 * branches without a license are ignored: if all other branches end up
	 * being consistent, the single license used for them is the obvious choice
	 * for all branches without a license.
	 * 
	 * @see LicenseInfo#getReplacementLicense()
	 * @see #getPrimaryLicenseFile()
	 * @see LicenseInfo#getEffectiveLicense()
	 */
	@JsonProperty("consistent")
	public boolean hasConsistentLicenses() {
		final Set<License> replacementLicenses = new HashSet<>();
		final Set<License> effectiveLicenses = new HashSet<>();
		boolean keep = false, missing = false;
		for (final LicenseInfo b : branches) {
			final License replacement = b.getReplacementLicense();
			final License eff = b.getEffectiveLicense();
			if (eff != null) {
				effectiveLicenses.add(eff);
				if (replacement != null)
					replacementLicenses.add(replacement);
				else
					keep = true;
			} else
				missing = true;
		}
		
		// equivalently: ignoring all branches without a license, eithers
		// - no branch has a license
		if (replacementLicenses.size() == 0 && !keep)
			return true;
		// - there is a single replacement license for everything (no keeps)
		if (replacementLicenses.size() == 1 && !keep)
			return true;
		// - there is no replacement license, and all keeps end up keeping the
		// same file (namely the primary license file)
		if (replacementLicenses.size() == 0 && getPrimaryLicenseFile() != null)
			return true;
		// - there is no replacement license, all keeps end up keeping the same
		// license, and there are no missing licenses
		if (replacementLicenses.size() == 0 && effectiveLicenses.size() == 1
				&& !missing)
			return true;

		// we get here if
		// - 2+ replacement licenses
		// - keep combined with replacement license(s)
		// - keep with multiple license files and multiple effective licenses
		// - keep with multiple license files and missing licenses
		return false;
	}

	/**
	 * <code>true</code> if there is any branch that doesn't have an
	 * autodetected license file
	 */
	@JsonProperty("missing")
	public boolean hasMissingLicenses() {
		for (final LicenseInfo b : branches)
			if (b.detectedFile == null)
				return true;
		return false;
	}

	/** <code>true</code> if more than one license was detected */
	@JsonProperty("multiple")
	public boolean hasMultipleLicenses() {
		return licensesSet.size() > 1;
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

	/**
	 * Returns the {@link LicenseFile} applicable to all branches, or
	 * <code>null</code> if there is no license that can be meaningfully
	 * considered to be applicable to all branches.
	 */
	@JsonIgnore
	public LicenseFile getPrimaryLicenseFile() {
		if (licensesSet.size() != 1)
			return null;
		return licensesSet.iterator().next();
	}

	/**
	 * Returns the {@link License} corresponding to
	 * {@link #getPrimaryLicenseFile()}, or <code>null</code> if there is no
	 * "primary" license. Note that the returned {@link License} will be
	 * {@link LicensesInfo#UNRECOGNIZED_LICENSE} if the license isn't in the
	 * database!
	 */
	@JsonProperty("primary")
	public License getPrimaryLicense() {
		final LicenseFile primary = getPrimaryLicenseFile();
		if (primary == null)
			return null;
		return supportedMap.get(primary.licenseID);
	}

	@JsonIgnore
	public LicenseInfo getLicense(final Ref ref) {
		for (final LicenseInfo b : branches)
			if (b.ref.equals(ref))
				return b;
		throw new NoSuchElementException("no license for branch " + ref.path);
	}

	public class LicenseInfo {
		/** {@link Ref} to which this license applies */
		@JsonProperty("ref")
		public final Ref ref;
		/**
		 * name of the file containing the license; <code>null</code> if file
		 * missing
		 */
		@JsonIgnore
		private final LicenseFile detectedFile;
		@JsonProperty("user")
		public final String user;

		public LicenseInfo(final Ref ref, final LicenseFile detected,
				final String user) {
			this.ref = ref;
			this.detectedFile = detected;
			this.user = user;
		}

		/**
		 * {@link LicenseFile} of the license to keep, or <code>null</code> if
		 * there is no obvious license to keep.
		 */
		@JsonIgnore
		public LicenseFile getLicenseFileToKeep() {
			if (detectedFile != null)
				return detectedFile;
			return getPrimaryLicenseFile();
		}

		/**
		 * The {@link License} corresponding to {@link #getLicenseFileToKeep()},
		 * or <code>null</code> if there is no obvious license to keep. Note
		 * that the returned {@link License} will be
		 * {@link LicensesInfo#UNRECOGNIZED_LICENSE} if the license isn't in the
		 * database!
		 */
		@JsonProperty("keep")
		public License getLicenseToKeep() {
			final LicenseFile keep = getLicenseFileToKeep();
			if (keep == null)
				return null;
			return getLicense(keep.licenseID);
		}

		/**
		 * {@link License} that was autodetected; <code>null</code> if file
		 * missing, or if the file contents aren't recognizable as one of the
		 * supported licenses
		 */
		@JsonProperty("detected")
		public final License getDetectedLicense() {
			if (detectedFile == null)
				return null;
			return getLicense(detectedFile.licenseID);
		}

		/**
		 * {@link License} to replace an existing LICENSE file with (if any), or
		 * <code>null</code> to keep the existing license.
		 */
		@JsonProperty("replacement")
		public License getReplacementLicense() {
			if (user == null)
				return null;
			return getLicense(user);
		}

		private License getLicense(String id) {
			if (!supportedMap.containsKey(id))
				// this really shouldn't happen. there's a foreign key
				// constraint enforcing only supported values in the license
				// field...
				throw new IllegalStateException(
						"new license " + id + " for branch " + ref.path
								+ " not present in database!");
			// if id == null, this uses the null key added in the LicensesInfo
			// constructor and sets detectedLicense = UNRECOGNIZED_LICENSE
			return supportedMap.get(id);
		}

		/**
		 * returns the ID of the license that was effectively selected. this is
		 * the user's choice if there is one, or the detected license if the
		 * user chose to keep the detected license (possibly
		 * {@link LicensesInfo#UNRECOGNIZED_LICENSE} if the license file is
		 * present but cannot be recognized as one of the supported licenses),
		 * or <code>null</code> if the user made no choice and no license was
		 * detected.
		 */
		@JsonProperty("effective")
		public License getEffectiveLicense() {
			final License replace = getReplacementLicense();
			if (replace != null)
				return replace;
			final License keep = getLicenseToKeep();
			if (keep != null)
				return keep;
			return null;
		}
	}
}