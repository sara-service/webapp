package bwfdm.sara.project;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BasicMetaData {
	@JsonProperty
	public final MetaDataItem title = new MetaDataItem();
	@JsonProperty
	public final MetaDataItem description = new MetaDataItem();
	@JsonProperty
	public final MetaDataItem version = new MetaDataItem();
	@JsonProperty
	public final MetaDataItem license = new MetaDataItem();
	@JsonProperty
	public final MetaDataItem sourceBranch = new MetaDataItem();

	public static class MetaDataItem {
		private String value;
		private boolean auto = true;

		private MetaDataItem() {
		}

		@JsonProperty("value")
		public String getValue() {
			return value;
		}

		@JsonProperty("autodetected")
		public boolean isAutoDetected() {
			return auto;
		}

		public void setValue(final String value, final boolean autoDetected) {
			this.value = value;
			auto = autoDetected;
		}
	}
}