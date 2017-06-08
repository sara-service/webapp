package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.git.ProjectInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/meta")
public class Meta {
	@Autowired
	private Repo repo;

	@GetMapping("")
	public BasicMetaData getBasicMetaData(final HttpSession session) {
		final ProjectInfo info = repo.getProjectInfo(session);
		String title = (String) session.getAttribute("title"); // TODO database
		if (title == null)
			title = info.name;
		return new BasicMetaData(title, info.description, "1.0", "none");
	}

	@PutMapping("title")
	public void setTitle(@RequestParam("value") final String title,
			final HttpSession session) {
		System.out.println("setTitle >" + title + "<");
		session.setAttribute("title", title); // TODO database
	}

	@DeleteMapping("title")
	public void unsetTitle(final HttpSession session) {
		System.out.println("unsetTitle");
		session.setAttribute("title", null); // TODO database
	}

	@PutMapping("description")
	public void setDescription(@RequestParam("value") final String description,
			final HttpSession session) {
		System.out.println("setdescription >" + description + "<");
		session.setAttribute("description", description); // TODO database
	}

	@DeleteMapping("description")
	public void unsetDescription(final HttpSession session) {
		System.out.println("unsetdescription");
		session.setAttribute("description", null); // TODO database
	}

	private static class BasicMetaData {
		@JsonProperty
		final String title;
		@JsonProperty
		final String description;
		@JsonProperty
		final String version;
		@JsonProperty
		final String license;

		private BasicMetaData(final String title, final String description,
				final String version, final String license) {
			this.title = title;
			this.description = description;
			this.version = version;
			this.license = license;
		}
	}
}
