package bwfdm.sara.git.gitlab;

import java.util.Date;

import bwfdm.sara.git.Commit;
import bwfdm.sara.git.DataObject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** data class for commits returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GLCommit implements DataObject<Commit> {
	/** commit hash */
	@JsonProperty("id")
	String id;
	/** first (summary) line of commit message. */
	@JsonProperty("title")
	String title;
	/** commit timestamp. */
	@JsonProperty("committed_date")
	@JsonFormat(pattern = GitLabRESTv4.DATE_FORMAT_PATTERN)
	Date date;

	@Override
	public Commit toDataObject() {
		return new Commit(id, title, date);
	}
}
