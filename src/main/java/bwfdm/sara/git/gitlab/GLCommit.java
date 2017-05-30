package bwfdm.sara.git.gitlab;

import java.util.Date;

import bwfdm.sara.git.Commit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for commits returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLCommit implements Commit {
	/** commit hash */
	@JsonProperty("id")
	private String id;
	/** first (summary) line of commit message. */
	@JsonProperty("title")
	private String title;
	/** commit timestamp. */
	@JsonProperty("committed_date")
	@JsonFormat(pattern = GitLabREST.DATE_FORMAT_PATTERN)
	private Date date;

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getTitle() {
		return title;
	}
}
