package bwfdm.sara.extractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.universalchardet.UniversalDetector;

import bwfdm.sara.extractor.licensee.LicenseeExtractor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.Ref.RefType;
import bwfdm.sara.transfer.RepoFile;
import bwfdm.sara.transfer.RepoFile.FileType;
import bwfdm.sara.transfer.TransferRepo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataExtractor {
	private static final Charset UTF8 = Charset.forName("UTF8");

	public static final String VERSION_FILE = "VERSION";

	private final Map<MetadataField, String> meta = new EnumMap<>(
			MetadataField.class);
	private final Map<String, String> versions = new HashMap<>();
	private final TransferRepo repo;
	private final GitProject project;

	public MetadataExtractor(final TransferRepo repo, final GitProject project) {
		this.repo = repo;
		this.project = project;
	}

	public Map<MetadataField, String> get(final MetadataField... fields) {
		final Map<MetadataField, String> res = new EnumMap<>(
				MetadataField.class);
		for (final MetadataField f : fields)
			res.put(f, meta.get(f));
		return res;
	}

	public void detectProjectInfo() {
		final ProjectInfo info = project.getProjectInfo();
		meta.put(MetadataField.TITLE, info.name);
		meta.put(MetadataField.DESCRIPTION, info.description);
	}

	private String readAsString(final String ref, final String filename)
			throws IOException {
		final byte[] blob = repo.getBlob(ref, filename);
		if (blob == null)
			return null;

		final UniversalDetector det = new UniversalDetector(null);
		det.handleData(blob, 0, blob.length);
		det.dataEnd();
		final String charset = det.getDetectedCharset();
		if (charset == null)
			// bug / peculiarity in juniversalchardet: if the input is ASCII, it
			// doesn't detect anything and returns null.
			// workaround by falling back to UTF-8 if nothing detected. in that
			// situation, it's the best guess anyway.
			return new String(blob, UTF8);
		return new String(blob, charset);
	}

	public List<LicenseInfo> detectLicenses() throws IOException {
		final List<LicenseInfo> licenses = new ArrayList<>();
		for (final String ref : repo.getRefs()) {
			final LicenseFile license = detectLicense(ref);
			if (license != null)
				licenses.add(new LicenseInfo(ref, license));
		}
		return licenses;
	}

	private LicenseFile detectLicense(final String ref) throws IOException {
		final List<RepoFile> files = repo.getFiles(ref, "");
		final List<LazyFile> candidates = new ArrayList<LazyFile>(files.size());
		for (final RepoFile file : files)
			if (file.getType() == FileType.FILE) {
				// TODO if we see something cached, return immediately
				final String name = file.getName();
				candidates.add(new LazyFile() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public String getContent() throws IOException {
						return readAsString(ref, name);
					}
				});
			}
		if (candidates.isEmpty())
			return null;
		final LicenseFile res = LicenseeExtractor.getInstance().detectLicense(
				candidates);
		// TODO cache result by file hash
		return res;
	}

	/** data class for autodetected license info. */
	public static class LicenseInfo {
		@JsonProperty
		final String ref;
		@JsonProperty
		final String path;
		@JsonProperty
		final String license;

		LicenseInfo(final String ref, final LicenseFile lic) {
			this.ref = ref;
			path = lic.getFile();
			license = lic.getID();
		}

		@Override
		public String toString() {
			return license;
		}
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
			final long date = repo.getCommitDate(ref.path);
			if ((best.type == RefType.TAG && ref.type == RefType.BRANCH)
					|| (date > bestDate)) {
				best = ref;
				bestDate = date;
			}
		}
		return best;
	}

	public void detectVersion() throws IOException {
		for (final String ref : repo.getRefs()) {
			String data = readAsString(ref, VERSION_FILE);
			if (data == null)
				data = "1.0"; // probably not a terrible guess
			versions.put(ref, data);
		}
	}

	public String setVersionFromBranch(final Ref ref) {
		final String version = versions.get(ref.path);
		meta.put(MetadataField.VERSION, version);
		return version;
	}

	public void updateVersion(final Ref ref, final String version) {
		versions.put(ref.path, version);
	}
}
