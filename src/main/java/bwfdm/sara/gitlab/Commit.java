package bwfdm.sara.gitlab;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for commits returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {
	/** commit hash */
	@JsonProperty("id")
	public String id;
	/** first (summary) line of commit message. */
	@JsonProperty("title")
	public String title;
	/** commit timestamp. */
	@JsonIgnore
	public Date date;

	@JsonProperty("committed_date")
	@JsonFormat(pattern = GitLabREST.DATE_FORMAT_PATTERN)
	public void setDate(final Date date) {
		this.date = date;
	}

	@JsonProperty("date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getDate() {
		return date;
	}
}
