package bwfdm.sara.transfer;

import bwfdm.sara.Config;
import bwfdm.sara.project.Project;
import bwfdm.sara.transfer.Task.TaskStatus;

public class TransferRepo {
	private final Project project;
	private final Config config;
	private CloneTask init;

	public TransferRepo(final Project project, final Config config) {
		this.project = project;
		this.config = config;
	}

	public void initialize() {
		// if there was an error, the background thread may still be removing
		// the old directory. some steps block for several seconds, so this is
		// not a difficult race to trigger at all.
		// therefore, just create a new CloneTask. that way, any race conditions
		// are moot anyway.
		if (init == null || init.isCancelled())
			init = new CloneTask(project.getGitProject(),
					project.getRefActions(), config);
		init.start();
	}

	public TaskStatus getStatus() {
		if (init != null)
			return init.getStatus();
		return null;
	}

	public void invalidate() {
		if (init != null)
			init.cancel();
		init = null;
	}

	public boolean isInitialized() {
		return init != null && init.isDone();
	}
}
