package bwfdm.sara.api;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.MetadataValue;
import bwfdm.sara.project.Project;

@RestController
@RequestMapping("/api/meta")
public class MetadataStorage {
	@GetMapping("")
	public Map<MetadataField, MetadataValue> getAllFields(
			final HttpSession session) {
		return Project.getInstance(session).getFrontendDatabase().getMetadata();
	}

	@GetMapping("{field}")
	public MetadataValue getSingleField(
			@PathVariable("field") final String name, final HttpSession session) {
		return getAllFields(session).get(MetadataField.forDisplayName(name));
	}

	@PutMapping("{field}")
	public void setSingleField(
			@PathVariable("field") final String name,
			@RequestParam("value") final String value,
			@RequestParam(name = "autodetected", defaultValue = "false") final boolean auto,
			final HttpSession session) {
		Project.getInstance(session).getFrontendDatabase()
				.setMetadata(MetadataField.forDisplayName(name), value, auto);
	}
}
