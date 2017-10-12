package bwfdm.sara.transfer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;

import bwfdm.sara.git.GitProject;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;

public class CloneTask extends Task {
	private final GitProject repo;
	private final Map<Ref, RefAction> actions;
	private final File root;
	private Git git;
	private final TransferRepo transferRepo;

	public CloneTask(final TransferRepo transferRepo, final GitProject repo,
			final Map<Ref, RefAction> actions) {
		this.transferRepo = transferRepo;
		this.repo = repo;
		this.actions = actions;
		root = transferRepo.getRoot();
	}

	@Override
	protected void cleanup() {
		transferRepo.dispose();
	}

	@Override
	protected void execute() throws GitAPIException, URISyntaxException,
			IOException {
		final Git git = initRepo();
		fetchHeads(git);
		pushBackHeads(git);
		transferRepo.setRepo(git.getRepository());
	}

	private Git initRepo() throws GitAPIException, URISyntaxException,
			IOException {
		beginTask("Initializing temporary repository", 1);
		final Git git = Git.init().setBare(true).setGitDir(root).call();
		// add the "origin" remote
		final RemoteAddCommand add = git.remoteAdd();
		add.setName(Constants.DEFAULT_REMOTE_NAME);
		add.setUri(new URIish(repo.getCloneURI()));
		final RemoteConfig remote = add.call();
		// change remote config to only fetch branches and tags that we actually
		// want.
		// due to limitations in the git clone protocol, we have to always
		// fetch the HEAD, even if we actually want to start archiving a few
		// commits back.
		final ArrayList<RefSpec> refs = new ArrayList<RefSpec>(actions.size());
		for (final Ref r : actions.keySet()) {
			final String path = Constants.R_REFS + r.path;
			refs.add(new RefSpec().setSourceDestination(path, path)
					.setForceUpdate(true));
		}
		remote.setFetchRefSpecs(refs);
		final StoredConfig config = git.getRepository().getConfig();
		remote.update(config);
		config.save();

		// deliberately not calling endTask() here: JGit will take a while until
		// it starts reporting the first step of the actual clone, and having a
		// checkmark sitting there with nothing happening is confusing.
		// instead just set the progress bar to 100%, without setting the
		// checkmark just yet. the next startTask() will do that anyway.
		update(1);
		return git;
	}

	private void fetchHeads(final Git git) throws GitAPIException,
			InvalidRemoteException, TransportException {
		final FetchCommand fetch = git.fetch();
		// to guard against corruption
		fetch.setCheckFetchedObjects(true);
		// TODO should we download submodules as well? if so, where do they go??
		fetch.setRecurseSubmodules(FetchRecurseSubmodulesMode.NO);
		// fetch tags iff they point to a ref somewhere along the history tree
		// that we're about to fetch. nicely deals with filtering.
		fetch.setTagOpt(TagOpt.AUTO_FOLLOW);

		// try-finally for best-effort attempt to never leave the repo in the
		// "can clone" state afterwards.
		repo.enableClone(true);
		try {
			repo.setCredentials(fetch);
			fetch.setProgressMonitor(this).call();
		} finally {
			repo.enableClone(false);
		}
	}

	private void pushBackHeads(final Git git) throws IOException {
		// if the user wanted to start archiving a few commits back, we now need
		// to push the HEAD backwards for these refs. it would be more efficient
		// to start fetching at those commit objects directly, but that isn't
		// possible in the git clone protocol.
		int pushBacks = 0;
		for (final RefAction r : actions.values())
			if (!r.getFirstCommit().equals(RefAction.HEAD_COMMIT))
				pushBacks++;
		if (pushBacks == 0)
			return; // nothing to do

		beginTask("Setting branch starting points", pushBacks);
		for (final Entry<Ref, RefAction> e : actions.entrySet()) {
			final String firstCommit = e.getValue().getFirstCommit();
			if (firstCommit.equals(RefAction.HEAD_COMMIT))
				continue;

			// TODO how to handle annotated tags?
			// as is, this turns annotated tags into lightweight tags unless
			// firstCommit == HEAD, losing whatever information was stored in
			// the annotated tag.
			// probably not a problem in practice:
			// - nobody uses annotated tags
			// - people who do probably don't push them backwards
			// - pointing signed annotated tags at a different commit would
			//   invalidate the signature so isn't advisable anyway
			final RefUpdate update = git.getRepository().updateRef(
					Constants.R_REFS + e.getKey().path);
			update.disableRefLog();
			update.setNewObjectId(ObjectId.fromString(firstCommit));
			update.setCheckConflicting(false);
			update.forceUpdate();
			update(1);
		}
		endTask();
	}

	public Repository getRepo() {
		if (!isDone())
			throw new IllegalStateException(
					"getRepo() on in-progress CloneTask");
		return git.getRepository();
	}
}