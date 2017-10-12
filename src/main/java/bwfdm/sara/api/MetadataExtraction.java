package bwfdm.sara.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.extractor.MetadataExtractor.LicenseInfo;
import bwfdm.sara.extractor.MetadataExtractor.VersionInfo;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.project.Project;
import bwfdm.sara.transfer.TransferRepo;

@RestController
@RequestMapping("/api/extract")
public class MetadataExtraction {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final MetadataExtractor meta;

	public MetadataExtraction() {
		meta = new MetadataExtractor();
		meta.initializeInBackground();
	}

	@GetMapping("licenses")
	public List<LicenseInfo> detectLicenses(final HttpSession session)
			throws IOException {
		final TransferRepo repo = Project.getInstance(session)
				.getTransferRepo();
		return meta.detectLicenses(repo);
	}

	@GetMapping("version")
	public VersionInfo detectVersion(@RequestParam("ref") final String ref,
			final HttpSession session) throws IOException {
		final TransferRepo repo = Project.getInstance(session)
				.getTransferRepo();
		return meta.detectVersion(repo, ref);
	}

	@PostMapping("version")
	public void updateVersion(@RequestParam("branch") final String branch,
			@RequestParam("version") final String version,
			final HttpSession session) {
		final GitProject repo = Project.getGitProject(session);
		// always write as UTF-8; that shouldn't break anything
		repo.putBlob(branch, MetadataExtractor.VERSION_FILE,
				"update version to " + version, version.getBytes(UTF8));
	}
}
