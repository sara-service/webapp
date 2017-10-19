package bwfdm.sara.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.MetadataValue;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.Ref.RefType;

@RestController
@RequestMapping("/api/meta")
public class Metadata {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	@GetMapping("")
	public Map<MetadataField, MetadataValue> getAllFields(
			final HttpSession session) {
		return Project.getInstance(session).getFrontendDatabase().getMetadata();
	}

	@GetMapping("{field}")
	public MetadataValue getSingleField(
			@PathVariable("field") final String name, final HttpSession session) {
		return Project.getInstance(session).getFrontendDatabase()
				.getMetadata(MetadataField.forDisplayName(name));
	}

	@PutMapping("{field}")
	public void setSingleField(@PathVariable("field") final String name,
			@RequestParam("value") final String value, final HttpSession session) {
		Project.getInstance(session).getFrontendDatabase()
				.setMetadata(MetadataField.forDisplayName(name), value);
	}

	private Map<MetadataField, MetadataValue> resetProjectInfo(
			final HttpSession session, final MetadataField... fields) {
		final Project project = Project.getInstance(session);
		// re-run detection. it's fast, and matches the real-time nature of the
		// update button: that one changes data in GitLab immediately, so the
		// user probably expects the reset button to pick up new values from
		// GitLab, too.
		final MetadataExtractor extractor = project.getMetadataExtractor();
		extractor.detectProjectInfo();

		// note the order: try to detect new value first, then delete what the
		// user entered. this way the field doesn't end up empty if detection
		// fails.
		final FrontendDatabase db = project.getFrontendDatabase();
		db.setAutodetectedMetadata(extractor.get(fields));
		for (final MetadataField field : fields)
			db.setMetadata(field, null);

		return getAllFields(session);
	}

	@PostMapping("title/reset")
	public Map<MetadataField, MetadataValue> resetTitle(
			final HttpSession session) {
		return resetProjectInfo(session, MetadataField.TITLE);
	}

	@PostMapping("title")
	public Map<MetadataField, MetadataValue> updateTitle(
			@RequestParam("value") final String title, final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getGitProject().updateProjectInfo(title, null);
		return resetTitle(session);
	}

	@PostMapping("description/reset")
	public Map<MetadataField, MetadataValue> resetDescription(
			final HttpSession session) {
		return resetProjectInfo(session, MetadataField.DESCRIPTION);
	}

	@PostMapping("description")
	public Map<MetadataField, MetadataValue> updateDescription(
			@RequestParam("value") final String description,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getGitProject().updateProjectInfo(null, description);
		return resetDescription(session);
	}

	@PostMapping("project-info/reset")
	public Map<MetadataField, MetadataValue> resetProjectInfo(
			final HttpSession session) {
		return resetProjectInfo(session, MetadataField.TITLE,
				MetadataField.DESCRIPTION);
	}

	@PostMapping("version/reset")
	public Map<MetadataField, MetadataValue> resetVersion(
			@RequestParam("ref") final String ref, final HttpSession session)
			throws IOException {
		final Project project = Project.getInstance(session);
		// need to re-run detection because it depends on the ref. this has the
		// side effect that it crashes if ref is invalid, so it's harder to get
		// garbage into the versionbranch field...
		final String version = project.getMetadataExtractor()
				.setVersionFromBranch(Ref.fromPath(ref));

		final FrontendDatabase db = project.getFrontendDatabase();
		db.setAutodetectedMetadata(MetadataField.VERSION, version);
		db.setMetadata(MetadataField.VERSION_BRANCH, ref);
		db.setMetadata(MetadataField.VERSION, null);

		return getAllFields(session);
	}

	@PostMapping("version")
	public Map<MetadataField, MetadataValue> updateVersion(
			@RequestParam("branch") final String branch,
			@RequestParam("value") final String version,
			final HttpSession session) {
		final Ref ref = new Ref(RefType.BRANCH, branch);
		final Project project = Project.getInstance(session);

		// always write as UTF-8; that shouldn't break anything
		project.getGitProject().putBlob(ref.name,
				MetadataExtractor.VERSION_FILE, "update version to " + version,
				version.getBytes(UTF8));
		// update MetadataExtractor's cached copy. the underlying repo won't
		// change, so re-running version detection would restore the old value,
		// which isn't desirable.
		project.getMetadataExtractor().updateVersion(ref, version);

		final FrontendDatabase db = project.getFrontendDatabase();
		db.setMetadata(MetadataField.VERSION_BRANCH, ref.path);
		db.setMetadata(MetadataField.VERSION, null);
		db.setAutodetectedMetadata(MetadataField.VERSION, version);
		// FIXME must update in local repo, too!
		// otherwise the "lazy" button won't detect it again

		return getAllFields(session);
	}
}
