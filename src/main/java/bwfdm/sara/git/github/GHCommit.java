package bwfdm.sara.git.github;

import java.util.Date;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.Commit;
import bwfdm.sara.git.DataObject;

/** data class for commits returned from GitLab. */
@JsonIgnoreProperties(ignoreUnknown = true)
class GHCommit implements DataObject<Commit> {
	private static final Pattern FIRST_LINE_ONLY = Pattern.compile("\n.*$",
			Pattern.DOTALL);

	/** commit hash */
	@JsonProperty("sha")
	String id;
	@JsonProperty("commit")
	GHCommitMeta commit;

	private static class GHCommitMeta {
		/** commit message (probably all of it). */
		@JsonProperty("message")
		String message;
		@JsonProperty("committer")
		GHCommitCommitter committer;
	}

	private static class GHCommitCommitter {
		/** commit timestamp. */
		@JsonProperty("date")
		@JsonFormat(pattern = GitHubRESTv3.DATE_FORMAT_PATTERN)
		Date date;
	}

	@Override
	public Commit toDataObject() {
		return new Commit(id,
				FIRST_LINE_ONLY.matcher(commit.message).replaceFirst(""),
				commit.committer.date);
	}
}
