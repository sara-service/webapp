package bwfdm.sara.git.local;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.TransportCommand;

import bwfdm.sara.git.ArchiveProject;

public class LocalArchiveProject implements ArchiveProject {
	private static final Log logger = LogFactory
			.getLog(LocalArchiveProject.class);

	private final File tempDir, targetDir;
	private boolean committed, deleted;
	private String webURL;

	LocalArchiveProject(final String webURL, final File tempDir,
			final File targetDir) {
		this.webURL = webURL;
		this.tempDir = tempDir;
		this.targetDir = targetDir;
	}

	@Override
	public String getWebURL() {
		return webURL;
	}

	@Override
	public String getPushURI() {
		return tempDir.getAbsolutePath();
	}

	@Override
	public void configureCredentials(final TransportCommand<?, ?> tx) {
		return;
	}

	@Override
	public void setDefaultBranch(final String defaultBranch) {
		// default branch cannot be implemented. plain git has all branches
		// being equal.
		// some branches being more equal than others is a gitlab / github
		// thing.
	}

	@Override
	public void rollback() {
		if (committed)
			throw new IllegalStateException(
					"attempt to delete committed project " + targetDir);
		if (deleted)
			return;

		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (final IOException e) {
			logger.error("failed to delete temp project " + tempDir, e);
		}
		deleted = true;
	}

	@Override
	public void commit() throws IOException {
		if (deleted)
			throw new IllegalStateException(
					"attempt to commit deleted project " + tempDir);
		if (committed)
			return;

		FileUtils.moveDirectory(tempDir, targetDir);
		committed = true;
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}
}
