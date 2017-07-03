package bwfdm.sara.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.extractor.LazyFile;
import bwfdm.sara.extractor.LicenseExtractor;
import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.licensee.LicenseeExtractor;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.git.RepoFile;
import bwfdm.sara.git.RepoFile.FileType;
import bwfdm.sara.project.Ref;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/extract")
public class MetadataExtractor {
	private static final String VERSION_FILE = "VERSION";
	private static final Charset UTF8 = Charset.forName("UTF-8");

	@Autowired
	private Repository repository;

	private final LicenseExtractor licenseExtractor;

	public MetadataExtractor() {
		licenseExtractor = new LicenseeExtractor();
	}

	@GetMapping("licenses")
	public List<LicenseInfo> detectLicenses(final HttpSession session) {
		final GitRepo repo = GitRepoFactory.getInstance(session);
		final List<LicenseInfo> licenses = new ArrayList<>();
		for (final Ref ref : repository.getSelectedBranches(session)) {
			// TODO honor the branch starting point here
			final LicenseFile license = detectLicense(ref.path, repo);
			if (license != null)
				licenses.add(new LicenseInfo(ref, license));
		}
		return licenses;
	}

	private LicenseFile detectLicense(final String ref, final GitRepo repo) {
		final List<RepoFile> files = repo.getFiles(ref, "");
		final List<LazyFile> candidates = new ArrayList<LazyFile>(files.size());
		for (final RepoFile file : files)
			// TODO if we see something cached, return immediately
			if (file.getType() == FileType.FILE)
				candidates.add(new LazyFile(repo, ref, file));
		if (candidates.isEmpty())
			return null;
		final LicenseFile res = licenseExtractor.detectLicense(candidates);
		// TODO cache result
		return res;
	}

	/** data class for autodetected license info. */
	static class LicenseInfo {
		@JsonProperty
		final String ref;
		@JsonProperty
		final String path;
		@JsonProperty
		final String license;

		LicenseInfo(final Ref ref, final LicenseFile lic) {
			this.ref = ref.path;
			path = lic.getFile();
			license = lic.getID();
		}
	}

	@GetMapping("version")
	public VersionInfo detectVersion(@RequestParam("ref") final String ref,
			final HttpSession session) {
		final GitRepo repo = GitRepoFactory.getInstance(session);

		final byte[] blob = repo.getBlob(ref, VERSION_FILE);
		if (blob == null)
			return new VersionInfo(null, VERSION_FILE, true);

		// TODO use juniversalchardet
		final String data = new String(blob, UTF8);
		return new VersionInfo(data, VERSION_FILE, true);
	}

	@PostMapping("version")
	public void updateVersion(@RequestParam("branch") final String branch,
			@RequestParam("version") final String version,
			final HttpSession session) {
		final GitRepo repo = GitRepoFactory.getInstance(session);
		// always write as UTF-8; that shouldn't break anything
		repo.putBlob(branch, VERSION_FILE, "update version to " + version,
				version.getBytes(UTF8));
	}

	/** data class for autodetected version info. */
	static class VersionInfo {
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
