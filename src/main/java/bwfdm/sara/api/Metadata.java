package bwfdm.sara.api;

import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonCreator;
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
			@RequestParam(name = "ref", required = false) final String refPath,
			final HttpSession session) {
		final Ref ref = refPath != null ? new Ref(refPath) : null;
		return getAllFields(ref, Project.getInstance(session));
	}

	@PutMapping("")
	public void setMultipleFields(
			@RequestBody final Map<MetadataField, MetadataValue> values,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		final Map<MetadataField, String> userValues = new HashMap<MetadataField, String>();
		final Set<MetadataField> update = new HashSet<MetadataField>();
		for (MetadataField field : values.keySet()) {
			MetadataValue value = values.get(field);
			userValues.put(field, value.user);
			if (value.update)
				update.add(field);
		}

		FrontendDatabase db = project.getFrontendDatabase();
		db.setMetadata(userValues);
		db.setUpdateMetadata(update);
		project.invalidateMetadata();
	}

	public static Map<MetadataField, MetadataValue> getAllFields(Ref ref,
			final Project project) {
		FrontendDatabase db = project.getFrontendDatabase();
		final Map<MetadataField, String> userValues = db.getMetadata();
		final Set<MetadataField> update = db.getUpdateMetadata();
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
			res.put(f, new MetadataValue(auto, user, update.contains(f)));
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
		/**
		 * <code>true</code> if the user selected the "update in git repo"
		 * checkbox
		 */
		@JsonProperty("update")
		public final boolean update;

		@JsonCreator
		public MetadataValue(@JsonProperty("value") final String value,
				@JsonProperty("update") final boolean update) {
			this.user = this.value = value;
			this.autodetected = null;
			this.update = update;
		}

		public MetadataValue(final String autodetected, final String user,
				final boolean update) {
			this.autodetected = autodetected;
			if (user != null) {
				this.user = user;
				this.update = update;
				value = user;
			} else {
				this.user = null;
				this.update = false;
				if (autodetected != null)
					value = autodetected;
				else
					value = "";
			}
		}
	}
}
