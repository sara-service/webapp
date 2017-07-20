package bwfdm.sara.extractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.universalchardet.UniversalDetector;

import bwfdm.sara.extractor.RepoFile.FileType;
import bwfdm.sara.extractor.licensee.LicenseeExtractor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataExtractor {
	private static final Charset UTF8 = Charset.forName("UTF8");

	public static final String VERSION_FILE = "VERSION";

	private final LicenseExtractor licenseExtractor;

	public MetadataExtractor() {
		licenseExtractor = new LicenseeExtractor();
	}

	private String readAsString(final LocalRepo repo, final String ref,
			final String filename) throws IOException {
		final byte[] blob = repo.getBlob(ref, filename);
		if (blob == null)
			return null;

		final UniversalDetector det = new UniversalDetector(null);
		det.handleData(blob, 0, blob.length);
		det.dataEnd();
		final String charset = det.getDetectedCharset();
		if (charset == null)
			// bug in juniversalchardet: if the input is ASCII, it doesn't
			// detect anything.
			// workaround by falling back to UTF-8 if nothing detected. it's the
			// best alternative anyway.
			return new String(blob, UTF8);
		return new String(blob, charset);
	}

	public List<LicenseInfo> detectLicenses(final LocalRepo repo)
			throws IOException {
		final List<LicenseInfo> licenses = new ArrayList<>();
		for (final String ref : repo.getRefs()) {
			final LicenseFile license = detectLicense(repo, ref);
			if (license != null)
				licenses.add(new LicenseInfo(ref, license));
		}
		return licenses;
	}

	private LicenseFile detectLicense(final LocalRepo repo, final String ref)
			throws IOException {
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
						return readAsString(repo, ref, name);
					}
				});
			}
		if (candidates.isEmpty())
			return null;
		final LicenseFile res = licenseExtractor.detectLicense(candidates);
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
	}

	public VersionInfo detectVersion(final LocalRepo repo, final String ref)
			throws IOException {
		final String data = readAsString(repo, ref, VERSION_FILE);
		// note: deliberately returns data == null when file missing!
		return new VersionInfo(data, VERSION_FILE, true);
	}

	/** data class for autodetected version info. */
	public static class VersionInfo {
		@JsonProperty
		final String version;
		@JsonProperty
		final String path;
		@JsonProperty
		final boolean canUpdate;

		VersionInfo(final String version, final String path,
				final boolean canUpdate) {
			this.version = version;
			this.path = path;
			this.canUpdate = canUpdate;
		}
	}
}
