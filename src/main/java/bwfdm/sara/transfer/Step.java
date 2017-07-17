package bwfdm.sara.transfer;

import org.eclipse.jgit.lib.ProgressMonitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
class Step {
	@JsonProperty
	public final String text;
	@JsonProperty
	private Status status = Status.PENDING;
	@JsonIgnore
	private int totalWork;
	@JsonIgnore
	private int currentWork;

	public void start(final int totalWork) {
		this.totalWork = totalWork;
		status = Status.WORKING;
	}

	public void update(final int completed) {
		currentWork += completed;
	}

	public void end() {
		status = Status.DONE;
	}

	public Step(final String text) {
		this.text = text;
	}

	@JsonProperty
	public float getProgress() {
		if (status == Status.DONE)
			return 1f;
		if (status == Status.PENDING || currentWork == 0)
			return 0f;

		// step is still active so it has a meaningful progress
		if (totalWork != ProgressMonitor.UNKNOWN)
			return currentWork / (float) totalWork;
		// calculate a fake progress that asymptotically approaches 1 as
		// currentWork approaches infinity. this means it will continue
		// making progress, but will never quite reach 100% until the step
		// is done.
		// note that this doesn't work for currentWork == 0, so that case is
		// handled above
		return 1 - 1 / (float) Math.sqrt(currentWork);
	}

	@Override
	public String toString() {
		return text + " " + status + " @" + getProgress();
	}

	private enum Status {
		@JsonProperty("pending")
		PENDING, //
		@JsonProperty("working")
		WORKING, //
		@JsonProperty("done")
		DONE
	}
}