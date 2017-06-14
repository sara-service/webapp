package bwfdm.sara.api;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/meta")
public class MetadataStorage {
	private String get(final HttpSession session, final String name,
			final boolean maskAuto) {
		final Object auto = session.getAttribute(name + ".auto");
		if (auto == null || ((boolean) auto && maskAuto))
			return null;
		return (String) session.getAttribute(name); // TODO database
	}

	private void set(final HttpSession session, final String name,
			final String value, final boolean auto) {
		session.setAttribute(name, value); // TODO database
		session.setAttribute(name + ".auto", auto); // TODO database
	}

	@GetMapping("")
	public BasicMetaData getBasicMetaData(
			@RequestParam(name = "mask-autodetected", defaultValue = "true") final boolean maskAuto,
			final HttpSession session) {
		final String title = get(session, "title", maskAuto);
		final String desc = get(session, "description", maskAuto);
		final String ver = get(session, "version", maskAuto);
		final String lic = get(session, "license", maskAuto);
		return new BasicMetaData(title, desc, ver, lic);
	}

	@PutMapping("title")
	public void setTitle(
			@RequestParam("value") final String title,
			@RequestParam(name = "autodetected", defaultValue = "false") final boolean auto,
			final HttpSession session) {
		set(session, "title", title, auto);
	}

	@PutMapping("description")
	public void setDescription(
			@RequestParam("value") final String description,
			@RequestParam(name = "autodetected", defaultValue = "false") final boolean auto,
			final HttpSession session) {
		set(session, "description", description, auto);
	}

	@PutMapping("version")
	public void setVersion(
			@RequestParam("value") final String version,
			@RequestParam(name = "autodetected", defaultValue = "false") final boolean auto,
			final HttpSession session) {
		set(session, "version", version, auto);
	}

	@PutMapping("license")
	public void setLicense(
			@RequestParam("value") final String license,
			@RequestParam(name = "autodetected", defaultValue = "false") final boolean auto,
			final HttpSession session) {
		set(session, "license", license, auto);
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
