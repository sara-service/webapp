package bwfdm.sara.extractor;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			final LicenseFile license = detectLicense(ref);
			if (license != null)
				licenses.put(ref, license);
		}
		return licenses;
	}

	private LicenseFile detectLicense(final Ref ref) throws IOException {
		final List<RepoFile> files = repo.getFiles(ref, "");
		for (final Iterator<RepoFile> iter = files.iterator(); iter.hasNext();) {
			final RepoFile file = iter.next();
			if (file.getType() != FileType.FILE)
				iter.remove();
			// TODO if we see something cached, return immediately
		}
		if (files.isEmpty())
			return null;

		final LicenseeExtractor extractor = LicenseeExtractor.getInstance();
		final LicenseFile res = extractor.detectLicense(repo, files);
		// TODO cache result by file hash
		return res;
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
				data = "1.0"; // probably not a terrible guess
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
