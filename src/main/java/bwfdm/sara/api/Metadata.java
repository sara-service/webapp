package bwfdm.sara.api;

import java.util.EnumMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.Ref;

@RestController
@RequestMapping("/api/meta")
public class Metadata {
	@GetMapping("")
	public Map<MetadataField, MetadataValue> getAllFields(
			@RequestParam(name = "ref", required = false) final String refPath,
			final HttpSession session) {
		final Ref ref = refPath != null ? new Ref(refPath) : null;
		return getAllFields(ref, Project.getInstance(session));
	}

	@PutMapping("")
	public void setMultipleFields(
			@RequestBody final Map<MetadataField, String> values,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getFrontendDatabase().setMetadata(values);
		project.invalidateMetadata();
	}

	public static Map<MetadataField, MetadataValue> getAllFields(Ref ref,
			final Project project) {
		FrontendDatabase db = project.getFrontendDatabase();
		final Map<MetadataField, String> userValues = db.getMetadata();
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
			res.put(f, new MetadataValue(auto, user));
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

		public MetadataValue(final String autodetected, final String user) {
			this.autodetected = autodetected;
			if (user != null) {
				this.user = user;
				value = user;
			} else {
				this.user = null;
				if (autodetected != null)
					value = autodetected;
				else
					value = "";
			}
		}
	}
}
