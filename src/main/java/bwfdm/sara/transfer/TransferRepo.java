package bwfdm.sara.transfer;

import java.io.File;

import bwfdm.sara.Config;
import bwfdm.sara.extractor.LocalRepo;
import bwfdm.sara.project.Project;
import bwfdm.sara.transfer.Task.TaskStatus;

public class TransferRepo {
	private final Project project;
	private final Config config;
	private CloneTask init;

	private File root;
	private LocalRepo repo;

	public TransferRepo(final Project project, final Config config) {
		this.project = project;
		this.config = config;
	}

	public void initialize() {
		if (init == null || init.isCancelled()) {
			// if there was an error, the background thread may still be
			// removing the old directory. some steps block for several seconds,
			// so this is not a difficult race to trigger at all. therefore,
			// just create a new CloneTask each time. that way, any race
			// conditions are moot.
			// use a unique directory each time so we don't have to worry about
			// a previous clone still shutting down when the next one starts
			root = config.getRandomTempDir();
			startClone();
		} else if (init.isDone())
			// if the user is triggering the clone again after it has finished,
			// perform another clone in the same directory. the repo might have
			// changed and the user almost certainly wants to see this change in
			// the archived data.
			startClone();
	}

	private void startClone() {
		init = new CloneTask(root, project.getGitProject(),
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

	public LocalRepo getRepo() {
		if (repo == null)
			repo = new LocalRepo(init.getRepo());
		return repo;
	}
}
