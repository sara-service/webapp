package bwfdm.sara.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import bwfdm.sara.extractor.licensee.LicenseeExtractor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.Ref.RefType;
import bwfdm.sara.transfer.RepoFile;
import bwfdm.sara.transfer.RepoFile.FileType;
import bwfdm.sara.transfer.TransferRepo;

public class MetadataExtractor {
	public static final String VERSION_FILE = "VERSION";
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
	private static final Pattern OBVIOUS_GLOBAL_LICENSE = Pattern.compile("^"
			+ LICENSE + EXTENSION + "$", Pattern.CASE_INSENSITIVE);
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

	private final Map<MetadataField, String> meta = new EnumMap<>(
			MetadataField.class);
	private final Map<String, String> versions = new HashMap<>();
	private final TransferRepo repo;
	private final GitProject project;
	private final Map<Ref, LicenseFile> licenses = new HashMap<>();

	public MetadataExtractor(final TransferRepo repo, final GitProject project) {
		this.repo = repo;
		this.project = project;
	}

	/**
	 * Get autodetected values for a defined set of {@link MetadataField}s. Pass
	 * {@link MetadataField#values()} to get all fields.
	 */
	public Map<MetadataField, String> get(final MetadataField... fields) {
		final Map<MetadataField, String> res = new EnumMap<>(
				MetadataField.class);
		for (final MetadataField f : fields)
			if (meta.containsKey(f))
				res.put(f, meta.get(f));
		return res;
	}

	public void detectProjectInfo() {
		final ProjectInfo info = project.getProjectInfo();
		meta.put(MetadataField.TITLE, info.name);
		meta.put(MetadataField.DESCRIPTION, info.description);
	}

	public Map<Ref, LicenseFile> detectLicenses(final Collection<Ref> refs)
			throws IOException {
		licenses.clear();
		for (final Ref ref : refs) {
			final LicenseFile license = detectLicenses(ref);
			if (license != null)
				licenses.put(ref, license);
		}
		return licenses;
	}

	private LicenseFile detectLicenses(final Ref ref) throws IOException {
		final List<RepoFile> obvious = new ArrayList<>();
		final List<RepoFile> likely = new ArrayList<>();
		// final List<RepoFile> possible = new ArrayList<>();
		final List<RepoFile> files = repo.getFiles(ref, "");
		for (final Iterator<RepoFile> iter = files.iterator(); iter.hasNext();) {
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

	private LicenseFile detectLicenses(final LicenseeExtractor extractor,
			final List<RepoFile> files) {
		final List<LicenseFile> licenses = extractor
				.detectLicenses(repo, files);
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

	public Map<Ref, LicenseFile> getLicenses() {
		return licenses;
	}

	public Ref detectMasterBranch(final Collection<Ref> refs)
			throws IOException {
		final Ref master = detectMaster(refs);
		meta.put(MetadataField.VERSION_BRANCH, master.path);
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
			final long date = repo.getHeadCommitDate(ref.path);
			if ((best.type == RefType.TAG && ref.type == RefType.BRANCH)
					|| (date > bestDate)) {
				best = ref;
				bestDate = date;
			}
		}
		return best;
	}

	public void detectVersion(final Set<Ref> set) throws IOException {
		for (final Ref ref : set) {
			String data = repo.readString(ref, VERSION_FILE);
			if (data == null)
				data = ""; // force the user to enter something
			versions.put(ref.path, data);
		}
	}

	public String setVersionFromBranch(final Ref ref) {
		final String version = versions.get(ref.path);
		meta.put(MetadataField.VERSION, version);
		return version;
	}

	public void updateVersion(final Ref ref, final String version) {
		// this will be overwritten if detectVersion() is called again, but
		// detectVersion() is only called after cloning and in that case we want
		// to update it anyway.
		versions.put(ref.path, version);
	}
}
