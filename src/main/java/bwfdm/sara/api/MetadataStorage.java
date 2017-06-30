package bwfdm.sara.api;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.project.BasicMetaData;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.BasicMetaData.MetaDataItem;

@RestController
@RequestMapping("/api/meta")
public class MetadataStorage {
	@GetMapping("")
	public BasicMetaData getAllFields(final HttpSession session) {
		return getMetadata(session);
	}

	private BasicMetaData getMetadata(final HttpSession session) {
		return Project.getInstance(session).getMetadata();
	}

	@GetMapping("{field}")
	public MetaDataItem getSingleField(
			@PathVariable("field") final String name, final HttpSession session) {
		final Field field;
		try {
			field = BasicMetaData.class.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new NoSuchElementException(name);
		}
		try {
			return (MetaDataItem) field.get(getMetadata(session));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@PutMapping("{field}")
	public void setSingleField(
			@PathVariable("field") final String name,
			@RequestParam("value") final String value,
			@RequestParam(name = "autodetected", defaultValue = "false") final boolean auto,
			final HttpSession session) {
		getSingleField(name, session).setValue(value, auto);
	}
}
