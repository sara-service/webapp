package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.project.ArchiveMetadata;
import bwfdm.sara.project.Project;

@RestController
@RequestMapping("/api/meta")
public class Metadata {
	@GetMapping("")
	public MetadataValues getAllFields(final HttpSession session) {
		final Project project = Project.getInstance(session);
		return new MetadataValues(project.getMetadataExtractor().getMetadata(),
				project.getFrontendDatabase().getMetadata());
	}

	@PutMapping("")
	public void setAllFields(@RequestBody final ArchiveMetadata values,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getFrontendDatabase().setMetadata(values);
		project.invalidateMetadata();
	}

	public static class MetadataValues {
		/**
		 * the "effective" value: the one the {@link #user} entered if present,
		 * else the {@link #autodetected} value.
		 */
		@JsonProperty("value")
		public final ArchiveMetadata value;
		/**
		 * the value the user entered, or <code>null</code> to use the
		 * {@link #autodetected} value.
		 */
		@JsonProperty("user")
		public final ArchiveMetadata user;
		/**
		 * the autodetected value, or <code>null</code> if nothing was detected.
		 */
		@JsonProperty("autodetected")
		public final ArchiveMetadata autodetected;

		public MetadataValues(final ArchiveMetadata auto,
				final ArchiveMetadata user) {
			this.autodetected = auto;
			if (user == null) {
				// must be non-null for JavaScript
				this.user = new ArchiveMetadata();
				value = auto;
			} else {
				this.user = user;
				value = auto.overrideFrom(user);
			}
		}
	}
}
