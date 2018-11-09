package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchiveMetadata {
	/** the publication title */
	@JsonProperty
	public String title;
	/** projects description / abstract */
	@JsonProperty
	public String description;
	/** software version number */
	@JsonProperty
	public String version;
	/** primary (default) branch in repo. */
	@JsonProperty
	public String master;
	/** name of the submitting user */
	@JsonProperty
	public Name submitter;

	public ArchiveMetadata() {
	}

	public ArchiveMetadata(final ArchiveMetadata orig) {
		title = orig.title;
		description = orig.description;
		version = orig.version;
		master = orig.master;
		submitter = orig.submitter;
	}

	/** Used by the database connection code only. */
	@JsonCreator
	public ArchiveMetadata(@JsonProperty("title") String title,
			@JsonProperty("description") String description,
			@JsonProperty("version") String version,
			@JsonProperty("master") String master,
			@JsonProperty("submitter_surname") String submitterSurname,
			@JsonProperty("submitter_givenname") String submitterGivenname) {
		this.title = title;
		this.description = description;
		this.version = version;
		this.master = master;
		this.submitter = new Name(submitterSurname, submitterGivenname);
	}

	/**
	 * Returns a new instance with non-<code>null</code> fields from
	 * {@code meta} overriding fields in {@code this}.
	 * 
	 * @param meta
	 *            overriding metadata
	 * @return a new {@link ArchiveMetadata} instance containing the value from
	 *         {@code meta} if that value is non-<code>null</code>, or the value
	 *         from {@code this} if not
	 */
	public ArchiveMetadata overrideFrom(final ArchiveMetadata meta) {
		final ArchiveMetadata res = new ArchiveMetadata(this);
		if (meta.title != null)
			res.title = meta.title;
		if (meta.description != null)
			res.description = meta.description;
		if (meta.version != null)
			res.version = meta.version;
		if (meta.master != null)
			res.master = meta.master;
		if (meta.submitter != null)
			res.submitter = meta.submitter;
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		final ArchiveMetadata meta = (ArchiveMetadata) obj;
		if (!meta.title.equals(title))
			return false;
		if (!meta.description.equals(description))
			return false;
		if (!meta.version.equals(version))
			return false;
		if (!meta.master.equals(master))
			return false;
		if (!meta.submitter.equals(submitter))
			return false;
		return true;
	}
}
