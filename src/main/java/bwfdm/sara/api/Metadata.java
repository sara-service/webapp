package bwfdm.sara.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.project.MetadataField;
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
		return getAllFields(Project.getInstance(session));
	}

	@PutMapping("")
	public void setAllFields(@RequestBody final Map<String, String> values,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		for (String name : values.keySet())
			project.getFrontendDatabase().setMetadata(
					MetadataField.forDisplayName(name), values.get(name));
		project.invalidateMetadata();
	}

	public static Map<MetadataField, MetadataValue> getAllFields(
			final Project project) {
		final Map<MetadataField, String> detectedValues = project
				.getMetadataExtractor().get(MetadataField.values());
		final Map<MetadataField, String> userValues = project
				.getFrontendDatabase().getMetadata();
		// make sure all fields are always present; JavaScript needs this. unset
		// values map to a {@code (null, null)} {@link MetadataValue}.
		final EnumMap<MetadataField, MetadataValue> res = new EnumMap<>(
				MetadataField.class);
		for (final MetadataField f : MetadataField.values()) {
			final String user = userValues.get(f);
			final String auto = detectedValues.get(f);
			res.put(f, new MetadataValue(user, auto));
		}
		return res;
	}

	@GetMapping("{field}")
	public MetadataValue getSingleField(
			@PathVariable("field") final String name, final HttpSession session) {
		return getAllFields(session).get(MetadataField.forDisplayName(name));
	}

	@PutMapping("{field}")
	public void setSingleField(@PathVariable("field") final String name,
			@RequestParam("value") final String value, final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getFrontendDatabase().setMetadata(
				MetadataField.forDisplayName(name), value);
		project.invalidateMetadata();
	}

	private Map<MetadataField, MetadataValue> resetProjectInfo(
			final HttpSession session, final MetadataField... fields) {
		final Project project = Project.getInstance(session);
		// not re-runnig detection here, to match the non-realtime behavior of
		// the "update" checkbox
		final FrontendDatabase db = project.getFrontendDatabase();
		for (final MetadataField field : fields)
			db.setMetadata(field, null);
		project.invalidateMetadata();

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
		project.getMetadataExtractor().setVersionFromBranch(new Ref(ref));

		final FrontendDatabase db = project.getFrontendDatabase();
		db.setMetadata(MetadataField.VERSION_BRANCH, ref);
		db.setMetadata(MetadataField.VERSION, null);
		project.invalidateMetadata();

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
		// note: this only works if detectVersion() is NOT called again here!
		// detectVersion() would overwrite this value!
		project.getMetadataExtractor().updateVersion(ref, version);

		final FrontendDatabase db = project.getFrontendDatabase();
		db.setMetadata(MetadataField.VERSION_BRANCH, ref.path);
		db.setMetadata(MetadataField.VERSION, null);
		project.invalidateMetadata(); // due to versionbranch field

		return getAllFields(session);
	}

	public static class MetadataValue {
		/**
		 * the "effective" value: the one the {@link #user} entered if present,
		 * else the {@link #autodetected} value.
		 */
		@JsonProperty("value")
		public final String value;
		/**
		 * the value the user entered, or <code>null</code> to use the
		 * {@link #autodetected} value.
		 */
		@JsonProperty("user")
		public final String user;
		/**
		 * the autodetected value, or <code>null</code> if nothing was detected.
		 */
		@JsonProperty("autodetected")
		public final String autodetected;

		private MetadataValue(final String user, final String autodetected) {
			this.user = user;
			this.autodetected = autodetected;
			if (user != null)
				value = user;
			else if (autodetected != null)
				value = autodetected;
			else
				value = "";
		}
	}
}
