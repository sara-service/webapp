package bwfdm.sara.transfer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;

import bwfdm.sara.extractor.MetadataExtractor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;
import bwfdm.sara.transfer.rewrite.HistoryRewriter;

public class CloneTask extends Task {
	private static final String EXTRACT_META = "Extracting metadata";
	private static final String ABBREV_HISTORY = "Abbreviating history";
	private static final String INIT_REPO = "Initializing temporary repository";
	private final MetadataExtractor extractor;
	private final GitProject project;
	private final List<RefAction> actions;
	private final List<Ref> refs;
	private final File root;
	private final TransferRepo transferRepo;
	private final boolean abbrev;
	private Git git;
	private Repository repo;

	public CloneTask(final TransferRepo transferRepo,
			final MetadataExtractor extractor, final GitProject project,
			final List<RefAction> actions) {
		this.transferRepo = transferRepo;
		this.extractor = extractor;
		this.project = project;
		this.actions = actions;
		refs = new ArrayList<>(actions.size());
		boolean abbrev = false;
		for (final RefAction a : actions) {
			refs.add(a.ref);
			if (a.publicationMethod != PublicationMethod.FULL)
				abbrev = true;
		}
		root = transferRepo.getRoot();
		declareSteps(INIT_REPO);
		if (abbrev)
			declareSteps(ABBREV_HISTORY);
		declareSteps(EXTRACT_META);
		this.abbrev = abbrev;
	}

	@Override
	protected void cleanup() {
		transferRepo.dispose();
		git = null;
		repo = null;
	}

	@Override
	protected void execute()
			throws GitAPIException, URISyntaxException, IOException {
		beginTask(INIT_REPO, 1);
		initRepo();
		deleteAllTags();
		fetchHeads();
		pushBackHeads();

		if (abbrev)
			rewriteHistory();

		transferRepo.setRepo(repo);
		extractMetaData();
		endTask();
	}

	private void initRepo()
			throws GitAPIException, URISyntaxException, IOException {
		git = Git.init().setBare(true).setGitDir(root).call();
		repo = git.getRepository();
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
		final ArrayList<RefSpec> spec = new ArrayList<RefSpec>(actions.size());
		for (final Ref r : refs) {
			final String path = Constants.R_REFS + r.path;
			spec.add(new RefSpec().setSourceDestination(path, path)
					.setForceUpdate(true));
		}
		remote.setFetchRefSpecs(spec);
		final StoredConfig config = repo.getConfig();
		remote.update(config);
		config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null,
				ConfigConstants.CONFIG_KEY_LOGALLREFUPDATES, true);
		config.save();

		// deliberately not calling endTask() here: JGit will take a while until
		// it starts reporting the first step of the actual clone, and having a
		// checkmark sitting there with nothing happening is confusing.
		// instead just set the progress bar to 100%, without setting the
		// checkmark just yet. the next startTask() will do that anyway.
		update(1);
	}

	private void deleteAllTags() throws IOException {
		// this method deletes all tags so that a subsequent fetchHeads() will
		// pick up on tags that have been deleted remotely. if we wouldn't
		// delete them here, they would stick around – which is bad since we're
		// using them to mark important commits in history.
		// techically, this can make objects unreachable, so if git's GC runs in
		// the meantime, they may end up being deleted and need to be fetched
		// again. in practice, we're right after a clone, so all objects are in
		// a single packfile, so GC will try to avoid repacking that. also, tags
		// are usually along the history, and thus their objects are still
		// reachable anyway.
		for (final org.eclipse.jgit.lib.Ref tag : repo.getTags().values()) {
			final RefUpdate update = repo.updateRef(tag.getName());
			update.setCheckConflicting(false);
			update.setForceUpdate(true);
			// it would be nice if that would actually log the tag update,
			// keeping the objects reachable. unfortunately it doesn't, neither
			// in jgit nor in cgit. it doesn't harm, though, so we might as well
			// keep it in.
			update.setRefLogMessage("SARA pre-commit cleanup", true);
			update.delete();
			checkUpdate(update);
		}
	}

	private void fetchHeads() throws GitAPIException {
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
		for (final RefAction r : actions)
			if (!r.firstCommit.equals(RefAction.HEAD_COMMIT))
				pushBacks++;
		if (pushBacks == 0)
			return; // nothing to do

		beginTask("Rewinding branches", pushBacks);
		for (final RefAction e : actions) {
			if (e.firstCommit.equals(RefAction.HEAD_COMMIT))
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
			final RefUpdate update = repo
					.updateRef(Constants.R_REFS + e.ref.path);
			update.setNewObjectId(ObjectId.fromString(e.firstCommit));
			update.setCheckConflicting(false);
			// log ref update to keep the old objects around. this can make
			// re-cloning the repo much faster because less objects have to be
			// transferred.
			// this silently does nothing for tags.
			update.setRefLogMessage("SARA rewind", true);
			update.forceUpdate();
			checkUpdate(update);
			update(1);
		}
	}

	private void rewriteHistory() throws IOException {
		final HistoryRewriter rewriter = new HistoryRewriter(repo);
		for (final RefAction action : actions)
			rewriter.addHead(Constants.R_REFS + action.ref.path,
					action.publicationMethod);

		beginTask(ABBREV_HISTORY, rewriter.getTotalSteps());
		rewriter.execute(this);

		for (final RefAction action : actions) {
			final String refPath = Constants.R_REFS + action.ref.path;
			if (rewriter.isUnchanged(refPath))
				continue;

			final RefUpdate update = repo
					.updateRef(Constants.R_REFS + action.ref.path);
			update.setCheckConflicting(false);
			update.setForceUpdate(true);
			// log ref update to keep the old objects around. this can make
			// re-cloning the repo much faster because less objects have to be
			// transferred.
			// this doesn't work for tags, but tags should be along the history.
			// also, the objects will stick around anyway, unless the GC happens
			// to run in between...
			update.setRefLogMessage("SARA rewrite", true);

			final RevCommit newCommit = rewriter.getRewrittenCommit(refPath);
			if (newCommit != null) {
				update.setNewObjectId(newCommit);
				update.update();
			} else
				update.delete();
			checkUpdate(update);
		}
	}

	static void checkUpdate(RefUpdate update) throws IOException {
		final Result res = update.getResult();
		switch (res) {
		case NO_CHANGE:
		case FAST_FORWARD:
		case FORCED:
			return;
		case NEW: // impossible if not creating new refs
		case RENAMED: // impossible if not renaming
		case REJECTED: // impossible if update forced
		case REJECTED_CURRENT_BRANCH: // impossible in bare repo
		case NOT_ATTEMPTED: // impossible if update() / delete() ever called
			throw new IllegalStateException(
					"'impossible' ref update/delete result " + res
							+ " updating " + update.getName());
		case IO_FAILURE:
		case LOCK_FAILURE:
			throw new IOException(
					"IO error " + res + " updating " + update.getName());
		}
		throw new UnsupportedOperationException(
				"ref update/delete result " + res);
	}

	private void extractMetaData() throws IOException {
		beginTask(EXTRACT_META, 2);
		extractor.detectMetaData(refs);
		update(1);
		extractor.detectLicenses(refs);
		update(1);
	}
}
