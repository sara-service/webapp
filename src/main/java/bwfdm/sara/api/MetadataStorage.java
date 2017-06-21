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
	private MetaDataItem get(final HttpSession session, final String name) {
		final Object auto = session.getAttribute(name + ".auto");
		if (auto == null)
			return new MetaDataItem("", true);
		final String value = (String) session.getAttribute(name);
		return new MetaDataItem(value, auto == null || (boolean) auto);
	}

	private void set(final HttpSession session, final String name,
			final String value, final boolean auto) {
		session.setAttribute(name, value); // TODO database
		session.setAttribute(name + ".auto", auto); // TODO database
	}

	@GetMapping("")
	public BasicMetaData getBasicMetaData(final HttpSession session) {
		final MetaDataItem title = get(session, "title");
		final MetaDataItem desc = get(session, "description");
		final MetaDataItem ver = get(session, "version");
		final MetaDataItem lic = get(session, "license");
		return new BasicMetaData(title, desc, ver, lic);
	}

	private static class BasicMetaData {
		@JsonProperty
		final MetaDataItem title;
		@JsonProperty
		final MetaDataItem description;
		@JsonProperty
		final MetaDataItem version;
		@JsonProperty
		final MetaDataItem license;

		private BasicMetaData(final MetaDataItem title,
				final MetaDataItem description, final MetaDataItem version,
				final MetaDataItem license) {
			this.title = title;
			this.description = description;
			this.version = version;
			this.license = license;
		}
	}

	private static class MetaDataItem {
		@JsonProperty
		final String value;
		@JsonProperty("autodetected")
		final boolean auto;

		private MetaDataItem(final String value, final boolean auto) {
			this.value = value;
			this.auto = auto;
		}
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
}
