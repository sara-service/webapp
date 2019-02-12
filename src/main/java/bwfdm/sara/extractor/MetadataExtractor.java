package bwfdm.sara.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bwfdm.sara.auth.AuthProvider.UserInfo;
import bwfdm.sara.extractor.licensee.LicenseeExtractor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.project.ArchiveMetadata;
import bwfdm.sara.project.Name;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.Ref.RefType;
import bwfdm.sara.transfer.RepoFile;
import bwfdm.sara.transfer.RepoFile.FileType;
import bwfdm.sara.transfer.TransferRepo;

public class MetadataExtractor {
	public static final String VERSION_FILE = "VERSION";
	private static final Pattern VERSION_REGEX = Pattern.compile("v(\\p{N}.*)");
	public static final String PREFERRED_LICENSE_FILE = "LICENSE";
	private static final String LICENSE = "((UN)?LICEN[SC]E|COPYING(\\.LESSER)?)";
	private static final String EXTENSION = "(\\.md|\\.markdown|\\.txt)?";
	/**
	 * regex for files whose existence implies that the they contain a license
	 * which almost certainly covers the entire project. the recommended name is
	 * {@code LICENSE} (or {@code COPYING} in old projects), but some licenses
	 * (Unlicense, LGPL 3.0) want to be different.
	 * <p>
	 * We probably shouldn't support everything here, but The Unlicense and the
	 * LGPL are just too important to ignore. Eg. the PostgreSQL license
	 * mentions {@code COPYRIGHT}, but it's not a very common license.
	 */
	private static final Pattern OBVIOUS_GLOBAL_LICENSE = Pattern
			.compile("^" + LICENSE + EXTENSION + "$", Pattern.CASE_INSENSITIVE);
	/**
	 * regex for other files that obviously contain a license. this is
	 * essentially just {@code LICENSE.*}, but because some projects (JRuby...)
	 * have files like {@code COPYING.RUBY} which only apply to parts of the
	 * project, we cannot directly imply that they cover the entire project. if
	 * there is just one of them, it should be safe though.
	 */
	// TODO should this include README.*? full text unlikely there
	private static final Pattern GLOBAL_LICENSE_OR_SUBLICENSE = Pattern
			.compile("^" + LICENSE + "\\..+$", Pattern.CASE_INSENSITIVE);

	private final ArchiveMetadata meta = new ArchiveMetadata();
	private final TransferRepo clone;
	private final GitProject project;
	private final GitRepo repo;
	private final Map<Ref, LicenseFile> licenses = new HashMap<>();
	private final Set<LicenseFile> licenseSet = new HashSet<>();
	private UserInfo userInfo;

	public MetadataExtractor(final TransferRepo clone, final GitRepo repo,
			final GitProject project) {
		this.clone = clone;
		this.repo = repo;
		this.project = project;
	}

	/**
	 * Get autodetected values for a defined {@link Ref}. Pass <code>null</code>
	 * to get branch-dependent fields for the default branch.
	 */
	public ArchiveMetadata get(final Ref ref) {
		final ArchiveMetadata res = new ArchiveMetadata(meta);
		// to add branch-specific metadata:
		// final String path = ref != null ? ref.path : meta.master;
		// then just add it
		return res;
	}

	/**
	 * The user's validated email address from project settings. Implementations
	 * of {@link GitRepo#getUserInfo()} must ensure the "validated" part.
	 */
	public String getEmail() {
		return userInfo.email;
	}

	/**
	 * The user's validated, unique user ID in that git repo. Implementations of
	 * {@link GitRepo#getUserInfo()} must ensure the "validated" and "unique"
	 * parts.
	 */
	public String getUserID() {
		return userInfo.userID;
	}

	/**
	 * Runs the main metadata detection. After this method, {@link #get(Ref)},
	 * {@link #getEmail()} and {@link #getUserID()} return valid information.
	 * 
	 * @param refs
	 *            set of refs to analyze
	 * @throws IOException
	 *             if repo access fails
	 */
	public void detectMetaData(final Collection<Ref> refs) throws IOException {
		detectProjectInfo();

		detectMasterBranch(refs);
		detectAuthors(refs);
		detectVersion();
		// for version-specific metadata, remember master, detect for all and
		// then set
		// meta.foo = foo.get(master);
	}

	private void detectProjectInfo() {
		final ProjectInfo info = project.getProjectInfo();
		userInfo = repo.getUserInfo();
		meta.title = info.name;
		meta.description = info.description;
		meta.submitter = userInfo.name;
	}

	private Ref detectMasterBranch(final Collection<Ref> refs)
			throws IOException {
		final Ref master = detectMaster(refs);
		meta.master = master.path;
		return master;
	}

	private Ref detectMaster(final Collection<Ref> refs) throws IOException {
		// pick the default branch from GitLab if it's in the list
		final String master = project.getProjectInfo().master;
		for (final Ref branch : refs)
			if (branch.type == RefType.BRANCH && branch.name.equals(master))
				return branch;

		// TODO if exactly 1 branch contains a LICENSE / COPYING file, use that

		// there isn't any obvious candidate, so we'll have to guess. let's just
		// pick the ref with the newest head commit; this will tend to at least
		// pick the newest version. prefer branches over tags again.
		Ref best = refs.iterator().next();
		long bestDate = Long.MIN_VALUE;
		for (final Ref ref : refs) {
			// FIXME 32-bit timestamp in an API! in 2017!!! wtf????
			final long date = clone.getCommit(ref).getCommitTime();
			if ((best.type == RefType.TAG && ref.type == RefType.BRANCH)
					|| (date > bestDate)) {
				best = ref;
				bestDate = date;
			}
		}
		return best;
	}

	private void detectAuthors(final Collection<Ref> set) throws IOException {
		final List<Name> authors = new ArrayList<>();
		// trivial heuristic: the submitter wrote all of it.
		// probably works just fine in a fairly large number of cases...
		// TODO implement something smarter (obviously)
		authors.add(userInfo.name);
		meta.setAuthors(authors);
	}

	private void detectVersion() throws IOException {
		// simply uses the tag that points to the most recent commit, as defined
		// by commit date. prefers tags that look like versions, ie. "v[0-9]*",
		// though it accepts digits in any script.
		Ref best = null;
		long bestDate = Long.MIN_VALUE;
		for (final Ref ref : clone.getTags()) {
			final long date = clone.getCommit(ref).getCommitTime();
			if (best == null
					|| (parseVersion(best) == null && parseVersion(ref) != null)
					|| date > bestDate) {
				best = ref;
				bestDate = date;
			}
		}

		final String version;
		if (best != null) {
			final String bestVersion = parseVersion(best);
			if (bestVersion != null)
				// an customary v1.2-style version number
				version = bestVersion;
			else
				// just an ordinary tag. who said versions have to be numeric?
				// "experiment-42" is better than nothing! (though it's nearly
				// impossible to sort that kind of version chronologically.)
				version = best.name;
		} else
			version = ""; // no version; force user to enter something
		meta.version = version;
	}

	private String parseVersion(final Ref ref) {
		final Matcher m = VERSION_REGEX.matcher(ref.name);
		if (!m.matches())
			return null;
		return m.group(1);
	}

	/**
	 * Gets the licenses for each ref. The map contains either the detected
	 * {@link LicenseFile} or <code>null</code> if no license file was detected
	 * in that branch.
	 * 
	 * @return a map containing all detected license files, per branch
	 */
	public Map<Ref, LicenseFile> getLicenses() {
		return licenses;
	}

	/**
	 * Gets the set of {@link LicenseFile} used across all refs. If there are
	 * several files containing the same license in slightly different variants
	 * (ie. different hash), all variants are returned. That is, the returned
	 * set can contain several versions of the same license, eg. two GPLs with
	 * different line endings.
	 * 
	 * @return the set of {@link LicenseFile} that were detected
	 */
	public Set<LicenseFile> getLicenseSet() {
		return licenseSet;
	}

	/**
	 * Runs the license detection. After this method, {@link #getLicenses()} and
	 * {@link #getLicenseSet()} return valid information.
	 * 
	 * @param refs
	 *            set of refs to analyze
	 * @throws IOException
	 *             if repo access fails
	 */
	public void detectLicenses(final Collection<Ref> refs) throws IOException {
		licenses.clear();
		licenseSet.clear();
		for (final Ref ref : refs) {
			final LicenseFile license = detectLicenses(ref);
			if (license != null) {
				licenses.put(ref, license);
				licenseSet.add(license);
			}
		}
	}

	private LicenseFile detectLicenses(final LicenseeExtractor extractor,
			final List<RepoFile> files) {
		final List<LicenseFile> licenses = extractor.detectLicenses(clone,
				files);
		// trivial case: only one license file, or missing license
		if (licenses.size() == 0)
			return null;
		if (licenses.size() == 1)
			return licenses.get(0);

		// multiple license files. even if only one of them matches, the others
		// could contain licenses we just don't know. treat as unrecognized
		// license.
		// TODO maybe we should pick the preferred file here?
		// the sets are in order of preference anyway, though
		final RepoFile file = files.get(0);
		return new LicenseFile(file, null, Float.NaN);
	}

	private LicenseFile detectLicenses(final Ref ref) throws IOException {
		final List<RepoFile> obvious = new ArrayList<>();
		final List<RepoFile> likely = new ArrayList<>();
		// final List<RepoFile> possible = new ArrayList<>();
		final List<RepoFile> files = clone.getFiles(ref);
		for (final Iterator<RepoFile> iter = files.iterator(); iter
				.hasNext();) {
			final RepoFile file = iter.next();
			if (file.getType() != FileType.FILE)
				continue;

			final String name = file.getName();
			if (OBVIOUS_GLOBAL_LICENSE.matcher(name).find())
				obvious.add(file);
			else if (GLOBAL_LICENSE_OR_SUBLICENSE.matcher(name).find())
				likely.add(file);
			// else
			// possible.add(file);
		}

		final LicenseeExtractor extractor = LicenseeExtractor.getInstance();
		if (!obvious.isEmpty())
			return detectLicenses(extractor, obvious);
		if (!likely.isEmpty())
			return detectLicenses(extractor, likely);
		// if (!possible.isEmpty())
		// return detectLicenses(extractor, possible);
		// there really isn't any license file in this branch
		return null;
	}
}
