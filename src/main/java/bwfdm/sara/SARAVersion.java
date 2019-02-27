package bwfdm.sara;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SARAVersion {
	@JsonProperty
	public final String version;
	@JsonProperty
	public final String branch;
	@JsonProperty
	public final String commit;
	@JsonProperty
	public final Date timestamp;

	// used by Jackson!
	SARAVersion(@JsonProperty("version") final String version,
			@JsonProperty("branch") final String branch,
			@JsonProperty("commit") final String commit,
			@JsonProperty("timestamp") final long timestamp) {
		this.version = version;
		this.branch = branch;
		this.commit = commit;
		this.timestamp = new Date(timestamp);
	}

	@Override
	public String toString() {
		return "SARA " + version + " (" + branch + "@" + commit.substring(0, 8)
				+ ")";
	}
}
