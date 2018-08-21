package bwfdm.sara.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.lib.ProgressMonitor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.api.Error.ErrorInfo;

public abstract class Task implements ProgressMonitor, Runnable {
	protected static final Log logger = LogFactory.getLog(Task.class);

	private final List<Step> steps = new ArrayList<>();
	private int lastCompletedIndex = -1;
	private Map<String, Step> declaredSteps;
	private Step currentStep;
	private boolean started, done, cancelled;
	private Exception exception;
	private Step failedStep;
	private Thread thread;

	protected void declareSteps(final List<String> steps) {
		if (declaredSteps == null)
			declaredSteps = new HashMap<>();
		for (final String t : steps) {
			final Step s = new Step(t);
			this.steps.add(s);
			declaredSteps.put(t, s);
		}
	}

	protected void declareSteps(final String... steps) {
		declareSteps(Arrays.asList(steps));
	}

	public synchronized Exception getException() {
		if (!done)
			throw new IllegalStateException(
					"getException while task still running");
		return exception;
	}

	public synchronized void start() {
		// start at most once
		if (started)
			return;
		thread = new Thread(this);
		thread.start();
		started = true;
	}

	@Override
	public void start(final int totalTasks) {
		return;
	}

	@Override
	public void endTask() {
		// ignored. users tend to get confused when a step is completed but the
		// next step hasn't started yet. thus we lie to them and delay ending
		// the step until the next one has already started.
	}

	@Override
	public void beginTask(final String title, final int totalWork) {
		// ending steps here instead of endTask(), see above
		endStep();
		currentStep = findTask(title, totalWork);
		currentStep.start(totalWork);
	}

	private void endStep() {
		if (currentStep != null)
			currentStep.end();
		currentStep = null;
	}

	private Step findTask(final String title, final int totalWork) {
		final Step step;
		if (declaredSteps != null && declaredSteps.containsKey(title)) {
			step = declaredSteps.get(title);
			lastCompletedIndex = steps.indexOf(step);
		} else {
			step = new Step(title);
			lastCompletedIndex++;
			steps.add(lastCompletedIndex, step);
		}
		return step;
	}

	@Override
	public void update(final int completed) {
		currentStep.update(completed);
	}

	/** @return <code>true</code> if the task has finished successfully */
	public boolean isDone() {
		return done && !cancelled;
	}

	/** @return <code>true</code> if the task has been cancelled or has crashed */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public void cancel() {
		if (thread != null)
			thread.interrupt();
		synchronized (this) {
			cancelled = true;
			if (!done)
				return; // thread will do it
		}
		// perform cleanup here if the thread has already exited
		cleanup();
	}

	@Override
	public final void run() {
		try {
			execute();
		} catch (final Exception e) {
			synchronized (this) {
				cancelled = true;
				exception = e;
				failedStep = currentStep;
			}
			logger.debug(e);
		} finally {
			synchronized (this) {
				// end the last step, finally setting checkmarks on everything
				endStep();
				done = true;
				if (!cancelled)
					return;
			}
			// if the task was cancelled while the thread was running, perform
			// cleanup here
			cleanup();
		}
	}

	protected abstract void cleanup();

	protected abstract void execute() throws Exception;

	public List<Step> getSteps() {
		return Collections.unmodifiableList(steps);
	}

	public TaskStatus getStatus() {
		return new TaskStatus();
	}

	@JsonInclude(Include.NON_NULL)
	public class TaskStatus {
		@JsonProperty
		private final StatusCode status;
		@JsonProperty
		private final List<Step> steps;
		@JsonProperty
		private final TaskErrorInfo error;

		private TaskStatus() {
			if (cancelled) {
				status = StatusCode.ERROR;
				if (exception != null) {
					logger.warn("exception in "
							+ Task.this.getClass().getSimpleName() + ": "
							+ exception.getClass().getSimpleName() + ": "
							+ exception.getMessage(), exception);
					final String fail = failedStep != null ? failedStep.text
							: null;
					error = new TaskErrorInfo(exception, fail);
				} else
					error = null;
			} else {
				error = null;
				status = done ? StatusCode.SUCCESS : StatusCode.ACTIVE;
			}
			steps = Task.this.steps;
		}
	}

	public enum StatusCode {
		@JsonProperty("active")
		ACTIVE, //
		@JsonProperty("success")
		SUCCESS, //
		@JsonProperty("error")
		ERROR
	}

	@JsonInclude(Include.NON_NULL)
	public static class TaskErrorInfo extends ErrorInfo {
		@JsonProperty
		public final String step;

		public TaskErrorInfo(final Exception e, final String step) {
			super(e);
			this.step = step;
		}
	}
}
