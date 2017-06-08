package bwfdm.sara.git;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Commit {
	/** commit hash */
	@JsonProperty("id")
	public final String id;

	/** first (summary) line of commit message. */
	@JsonProperty("title")
	public final String title;

	/** commit timestamp. */
	@JsonProperty("date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	public final Date date;

	public Commit(final String id, final String title, final Date date) {
		this.id = id;
		this.title = title;
		this.date = date;
	}
}
