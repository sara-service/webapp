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
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;

import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;

public class CloneTask extends Task {
	private final MetadataExtractor extractor;
	private final GitProject project;
	private final Map<Ref, RefAction> actions;
	private final File root;
	private final TransferRepo transferRepo;
	private final MetadataSink meta;
	private Git git;

	public CloneTask(final TransferRepo transferRepo,
			final MetadataExtractor extractor, final GitProject project,
			final Map<Ref, RefAction> actions, final MetadataSink meta) {
		this.transferRepo = transferRepo;
		this.extractor = extractor;
		this.project = project;
		this.actions = actions;
		this.meta = meta;
		root = transferRepo.getRoot();
	}

	@Override
	protected void cleanup() {
		transferRepo.dispose();
	}

	@Override
	protected void execute() throws GitAPIException, URISyntaxException,
			IOException {
		initRepo();
		fetchHeads();
		pushBackHeads();
		transferRepo.setRepo(git.getRepository());
		extractMetaData();
		endTask();
	}

	private void initRepo() throws GitAPIException, URISyntaxException,
			IOException {
		beginTask("Initializing temporary repository", 1);
		git = Git.init().setBare(true).setGitDir(root).call();
		// add the "origin" remote
		final RemoteAddCommand add = git.remoteAdd();
		add.setName(Constants.DEFAULT_REMOTE_NAME);
		add.setUri(new URIish(project.getCloneURI()));
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
	}

	private void fetchHeads() throws GitAPIException, InvalidRemoteException,
			TransportException {
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
		project.enableClone(true);
		try {
			project.setCredentials(fetch);
			fetch.setProgressMonitor(this).call();
		} finally {
			project.enableClone(false);
		}
	}

	private void pushBackHeads() throws IOException {
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
	}

	private void extractMetaData() throws IOException {
		beginTask("Extracting metadata", 3);
		extractor.detectProjectInfo();
		update(1);

		final Ref master = extractor.detectMasterBranch(actions.keySet());
		extractor.detectVersion(actions.keySet()); // detect for ALL branches
		extractor.setVersionFromBranch(master);
		update(1);

		meta.setAutodetectedMetadata(extractor.get(MetadataField.values()));

		final Map<Ref, LicenseFile> licenses = extractor.detectLicenses(actions
				.keySet());
		meta.setAutodetectedLicenses(licenses);
		update(1);
	}
}
