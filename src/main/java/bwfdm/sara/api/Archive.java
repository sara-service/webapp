package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.db.ArchiveAccess;
import bwfdm.sara.project.Project;

@RestController
@RequestMapping("/api/archive")
public class Archive {
	@GetMapping("")
	public ArchiveOptions getOptions(final HttpSession session) {
		final ArchiveOptions opts = new ArchiveOptions();
		opts.access = Project.getInstance(session).getFrontendDatabase()
				.getArchiveAccess();
		return opts;
	}

	@PutMapping("")
	public void setOptions(@RequestBody final ArchiveOptions opts,
			final HttpSession session) {
		Project.getInstance(session).getFrontendDatabase()
				.setArchiveAccess(opts.access);
	}

	// this is a bit silly right now, but is expected to grow to include archive
	// selection
	public static class ArchiveOptions {
		@JsonProperty
		public ArchiveAccess access;
	}
}
