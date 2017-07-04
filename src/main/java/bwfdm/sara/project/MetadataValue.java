package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataValue {
	@JsonProperty("value")
	public final String value;
	@JsonProperty("autodetected")
	public final boolean autodetected;

	public MetadataValue(final String value, final boolean autodetected) {
		this.value = value;
		this.autodetected = autodetected;
	}
}