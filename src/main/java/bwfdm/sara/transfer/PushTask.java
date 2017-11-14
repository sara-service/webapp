package bwfdm.sara.transfer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import bwfdm.sara.Config;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.ArchiveRepo.ProjectExistsException;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;

public class PushTask extends Task {
	private static final String TARGET_REMOTE = "target";

	private final Repository repo;
	private final Map<Ref, RefAction> actions;
	private final ArchiveRepo archive;
	private final Map<MetadataField, String> meta;
	private final boolean visible;
	private ArchiveProject project;

	public PushTask(final TransferRepo repo, final Map<Ref, RefAction> actions,
			final ArchiveRepo archive, final Map<MetadataField, String> meta,
			final boolean visible) {
		this.repo = repo.getRepo();
		this.actions = actions;
		this.archive = archive;
		this.meta = meta;
		this.visible = visible;
	}

	@Override
	protected void cleanup() {
		if (project == null || !project.isEmpty())
			return; // never remove projects that we didn't create!
		// TODO SARA user should not need this permission on the archive
		project.deleteProject();
		project = null;
	}

	@Override
	protected void execute() throws GitAPIException, URISyntaxException,
			IOException, ProjectExistsException {
		beginTask("NOT rewriting history (not implemented yet)", 1);
		// TODO handle abbreviated history etc, producing @version refs
		beginTask("NOT committing metadata to git archive"
				+ " (not implemented yet, either)", 1);
		// TODO commit submitted_metadata.xml to repo

		final String id = Config.getRandomID();
		beginTask("Creating project " + id, 1);
		project = archive.createProject(id, visible, meta);
		final String version = meta.get(MetadataField.VERSION);

		beginTask("Preparing repository for upload", 1);
		final Git git = Git.wrap(repo);
		final RemoteAddCommand add = git.remoteAdd();
		add.setName(TARGET_REMOTE);
		add.setUri(new URIish(project.getPushURI()));
		add.call();
		// final RemoteConfig remote = add.call();
		// again not calling endTask() here; push will take a while to connect
		// and get started
		update(1);

		final PushCommand push = git.push();
		final ArrayList<RefSpec> refs = new ArrayList<RefSpec>(actions.size());
		for (final Ref r : actions.keySet()) {
			final String path = Constants.R_REFS + r.path;
			// TODO send locally-created @version refs unchanged
			refs.add(new RefSpec().setSourceDestination(path,
					path + "@" + version).setForceUpdate(true));
		}
		// remote.setPushRefSpecs(refs);
		// final StoredConfig config = git.getRepository().getConfig();
		// remote.update(config);
		// config.save();
		push.setRefSpecs(refs);
		push.setRemote(TARGET_REMOTE);

		project.setCredentials(push);
		push.setProgressMonitor(this).call();
	}

	public String getWebURL() {
		if (!isDone())
			throw new IllegalStateException(
					"getWebURL() on in-progress PushTask");
		return project.getWebURL();
	}
}
