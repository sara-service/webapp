package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataValue {
	/**
	 * the "effective" value: the one the {@link #user} entered if present, else
	 * the {@link #autodetected} value.
	 */
	@JsonProperty("value")
	public final String value;
	/**
	 * the value the user entered, or <code>null</code> to use the
	 * {@link #autodetected} value.
	 */
	@JsonProperty("user")
	public final String user;
	/** the autodetected value, or <code>null</code> if nothing was detected. */
	@JsonProperty("autodetected")
	public final String autodetected;

	public MetadataValue(final String user, final String autodetected) {
		this.user = user;
		this.autodetected = autodetected;
		if (user != null)
			value = user;
		else if (autodetected != null)
			value = autodetected;
		else
			value = "";
	}
}