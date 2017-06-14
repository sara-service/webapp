package bwfdm.sara.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/extract")
public class MetadataExtractor {
	private static final String VERSION_FILE = "VERSION";
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final List<String> LICENSE_FILE_NAMES = Arrays.asList(
			"LICENSE", "COPYING");

	@GetMapping("licenses")
	public List<LicenseInfo> detectLicenses(final HttpSession session) {
		final String branch = "heads/master";// FIXME iterate all selected refs!
		final GitRepo repo = GitRepoFactory.getInstance(session);

		final List<LicenseInfo> licenses = new ArrayList<>();
		for (final String name : LICENSE_FILE_NAMES) {
			final byte[] blob = repo.getBlob(branch, name);
			if (blob != null)
				// TODO actually detect what license it is
				licenses.add(new LicenseInfo(branch, name, null));
		}
		return licenses;
	}

	/** data class for autodetected license info. */
	static class LicenseInfo {
		@JsonProperty
		final String ref;
		@JsonProperty
		final String path;
		@JsonProperty
		final String license;

		LicenseInfo(final String ref, final String path, final String license) {
			this.ref = ref;
			this.path = path;
			this.license = license;
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
