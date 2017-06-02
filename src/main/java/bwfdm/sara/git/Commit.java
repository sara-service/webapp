package bwfdm.sara.git;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface Commit {
	/** commit hash */
	@JsonProperty("id")
	public String getID();

	/** first (summary) line of commit message. */
	@JsonProperty("title")
	public String getTitle();

	/** commit timestamp. */
	@JsonProperty("date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getDate();
}
