package bwfdm.sara.api;

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
			@RequestParam(name = "ref", required = false) final String refPath,
			final HttpSession session) {
		final Ref ref = refPath != null ? new Ref(refPath) : null;
		return getAllFields(ref, Project.getInstance(session));
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

	public static Map<MetadataField, MetadataValue> getAllFields(Ref ref,
			final Project project) {
		final Map<MetadataField, String> userValues = project
				.getFrontendDatabase().getMetadata();
		// if the user has selected a different main branch than the
		// autodetected one, make sure we request the branch-specific metadata
		// for that branch.
		final String master = userValues.get(MetadataField.MAIN_BRANCH);
		if (master != null && ref == null)
			ref = new Ref(master);
		final Map<MetadataField, String> detectedValues = project
				.getMetadataExtractor().get(ref, MetadataField.values());
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
			@PathVariable("field") final String name,
			final HttpSession session) {
		return getAllFields(null, session)
				.get(MetadataField.forDisplayName(name));
	}

	@PutMapping("{field}")
	public void setSingleField(@PathVariable("field") final String name,
			@RequestParam("value") final String value,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getFrontendDatabase()
				.setMetadata(MetadataField.forDisplayName(name), value);
		project.invalidateMetadata();
	}

	/** @deprecated move to PushTask */
	@PostMapping("title")
	public void updateTitle(@RequestParam("value") final String title,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getGitProject().updateProjectInfo(title, null);
	}

	/** @deprecated move to PushTask */
	@PostMapping("description")
	public void updateDescription(
			@RequestParam("value") final String description,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getGitProject().updateProjectInfo(null, description);
	}

	/** @deprecated move to PushTask */
	@PostMapping("version")
	public void updateVersion(
			@RequestParam("branch") final String branch,
			@RequestParam("value") final String version,
			final HttpSession session) {
		final Ref ref = new Ref(RefType.BRANCH, branch);
		final Project project = Project.getInstance(session);

		// always write as UTF-8; that shouldn't break anything
		project.getGitProject().putBlob(ref.name,
				MetadataExtractor.VERSION_FILE, "update version to " + version,
				version.getBytes(UTF8));
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
