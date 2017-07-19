package bwfdm.sara.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.lib.ProgressMonitor;

import bwfdm.sara.project.ErrorInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Task implements ProgressMonitor, Runnable {
	private static final Log logger = LogFactory.getLog(TransferRepo.class);

	private final List<Step> steps = new ArrayList<>();
	private Map<String, Step> declaredSteps;
	private Step currentSteps;
	private boolean started, done, cancelled;
	private Exception exception;
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
	public void beginTask(final String title, final int totalWork) {
		endTask(); // JGit tends to not call this
		currentSteps = findTask(title, totalWork);
		currentSteps.start(totalWork);
	}

	private Step findTask(final String title, final int totalWork) {
		// if the list of tasks has been declared, we only allow these tasks to
		// be reported
		if (declaredSteps != null) {
			if (!declaredSteps.containsKey(title))
				throw new NoSuchElementException("unexpected step: " + title);
			return declaredSteps.get(title);
		}

		// if the list of tasks is unknown, each new task is simply appended to
		// the list
		// note that the frontend will always append new tasks to the end of the
		// displayed list, no matter where they are in the JSON list! thus
		// trying to insert them correctly here doesn't make a lot of sense.
		final Step step = new Step(title);
		steps.add(step);
		return step;
	}

	@Override
	public void update(final int completed) {
		currentSteps.update(completed);
	}

	@Override
	public void endTask() {
		if (currentSteps != null)
			currentSteps.end();
		currentSteps = null;
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
			}
			logger.debug(e);
		} finally {
			synchronized (this) {
				endTask(); // JGit tends to not call this
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
		private final ErrorInfo error;

		private TaskStatus() {
			if (cancelled) {
				status = StatusCode.ERROR;
				if (exception != null)
					error = new ErrorInfo(exception);
				else
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
}
