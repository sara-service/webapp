package bwfdm.sara.transfer;

import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;
import org.springframework.util.FileSystemUtils;

import bwfdm.sara.Config;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;

class CloneTask extends Task {
	private static final SecureRandom RNG = new SecureRandom();

	private final GitProject repo;
	private final Map<Ref, RefAction> actions;
	private final File root;
	private Git git;

	public CloneTask(final GitProject repo, final Map<Ref, RefAction> actions,
			final Config config) {
		this.repo = repo;
		this.actions = actions;
		// use something unique each time so we don't have to worry about a
		// previous clone still shutting down when the next one starts
		root = config.getTempDir(new BigInteger(80, RNG).toString());
	}

	@Override
	protected void cleanup() {
		FileSystemUtils.deleteRecursively(root);
	}

	@Override
	protected void execute() throws GitAPIException, URISyntaxException {
		beginTask("Initializing temporary repository", 1);
		final Git git = Git.init().setBare(true).setGitDir(root).call();
		final RemoteAddCommand add = git.remoteAdd();
		add.setName(Constants.DEFAULT_REMOTE_NAME);
		add.setUri(new URIish(repo.getCloneURI()));
		add.call();
		// deliberately not calling endTask() here: JGit will take a while until
		// it starts reporting the next step, and having a checkmark sitting
		// there with nothing happening is confusing. instead just set the
		// progress bar to 100%, without setting the checkmark just yet. the
		// next startTask() will do that anyway.
		update(1);

		final FetchCommand fetch = git.fetch();
		// to guard against corruption
		fetch.setCheckFetchedObjects(true);
		// TODO should we download submodules as well? if so, where do they go??
		fetch.setRecurseSubmodules(FetchRecurseSubmodulesMode.NO);
		// fetch tags iff they point to a ref somewhere along the history tree
		// that we're about to fetch. nicely deals with filtering.
		fetch.setTagOpt(TagOpt.AUTO_FOLLOW);
		// only fetch branches and tags that we actually want.
		// due to limitations in the git clone protocol, we have to always
		// fetch the HEAD, even if we actually want to start archiving a few
		// commits back.
		final ArrayList<RefSpec> specs = new ArrayList<RefSpec>(actions.size());
		for (final Ref r : actions.keySet())
			specs.add(new RefSpec("refs/" + r.path));
		fetch.setRefSpecs(specs);

		fetch.setCredentialsProvider(repo.getCloneCredentials());
		fetch.setProgressMonitor(this);
		repo.enableClone(true);
		try {
			fetch.call();
		} finally {
			repo.enableClone(false);
		}

		this.git = git;
	}

	public Git getRepo() {
		return git;
	}
}