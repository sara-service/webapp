package bwfdm.sara.transfer.rewrite;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import bwfdm.sara.project.RefAction.PublicationMethod;
import bwfdm.sara.transfer.TransferRepo;

public class HistoryRewriter {
	/** Refs whose full history is published. */
	private final Set<ObjectId> full = new HashSet<>();
	/** Refs whose history is published in abbreviated form. */
	private final Set<ObjectId> abbrev = new HashSet<>();
	/** Refs whose latest version only is published. */
	private final Set<ObjectId> latest = new HashSet<>();
	/**
	 * All refs that are explicitly marked for publication. Union of
	 * {@link #full}, {@link #abbrev} and {@link #latest}.
	 */
	private final Set<ObjectId> heads = new HashSet<>();
	private final RewriteCache cache = new RewriteCache();
	private final Repository repo;

	public HistoryRewriter(Repository repo) {
		this.repo = repo;
	}

	/**
	 * Marks a ref for rewriting by the defined method.
	 * 
	 * @param refPath
	 *            full ref path, including {@link Constants#R_REFS} prefix
	 * @param method
	 *            {@link PublicationMethod}, must not be <code>null</code>
	 */
	public void addHead(final String refPath, final PublicationMethod method)
			throws IOException {
		final ObjectId commit = TransferRepo.resolve(repo, refPath);
		heads.add(commit);

		switch (method) {
		case FULL:
			full.add(commit);
			break;
		case ABBREV:
			abbrev.add(commit);
			break;
		case LATEST:
			latest.add(commit);
			break;
		default:
			throw new UnsupportedOperationException(
					"PublicationMethod." + method);
		}
	}

	/**
	 * Performs rewriting.
	 * 
	 * @param progress
	 *            {@link ProgressMonitor} for user feedback, may be null
	 */
	public void execute(final ProgressMonitor progress) throws IOException {
		rewrite(full, new FullHistory(repo, cache), progress);
		rewrite(abbrev, new AbbreviatedHistory(repo, cache), progress);
		rewrite(latest, new LatestVersion(repo, cache), progress);
	}

	private void rewrite(final Set<ObjectId> heads,
			final RewriteStrategy strategy, final ProgressMonitor progress)
			throws IOException {
		try {
			for (final ObjectId head : heads) {
				strategy.process(repo.parseCommit(head));
				if (progress != null)
					progress.update(1);
			}
		} finally {
			strategy.close();
		}
	}

	/**
	 * @return total number of update steps reported by
	 *         {@link #execute(ProgressMonitor)}
	 */
	public int getTotalSteps() {
		return full.size() + abbrev.size() + latest.size();
	}

	/**
	 * Checks whether a ref still points to the same commit after rewriting. If
	 * a ref is unchanged, it should not be updated because updating loses
	 * annotated tags.
	 * 
	 * @param refPath
	 *            full ref path, including {@link Constants#R_REFS} prefix
	 * @return <code>true</code> if this ref still points to the same commit
	 */
	public boolean isUnchanged(final String refPath) throws IOException {
		final ObjectId orig = TransferRepo.resolve(repo, refPath);
		return orig.equals(cache.getRewrittenCommit(orig));
	}

	/**
	 * Get the new commit that this ref should point to.
	 * 
	 * @param refPath
	 *            full ref path, including {@link Constants#R_REFS} prefix
	 * @return the new commit, or <code>null</code> if the commit was removed
	 *         from the rewritten history
	 */
	public RevCommit getRewrittenCommit(final String refPath)
			throws IOException {
		final ObjectId rewritten = cache
				.getRewrittenCommit(TransferRepo.resolve(repo, refPath));
		if (rewritten != null)
			return repo.parseCommit(rewritten);
		return null;
	}

	private class AbbreviatedHistory extends FilteredHistory {
		private final Set<ObjectId> important;

		private AbbreviatedHistory(final Repository repo,
				final RewriteCache cache) throws IOException {
			super(repo, cache);
			important = new HashSet<>(heads);
			for (final Ref tag : repo.getTags().values())
				important.add(TransferRepo.resolve(repo, tag.getName()));
		}

		@Override
		protected boolean isSignificant(final RevCommit commit,
				final List<ObjectId> parents) {
			return important.contains(commit);
		}
	}

	private class LatestVersion extends FilteredHistory {
		private LatestVersion(final Repository repo, final RewriteCache cache) {
			super(repo, cache);
		}

		@Override
		protected boolean isSignificant(final RevCommit commit,
				final List<ObjectId> parents) {
			return heads.contains(commit);
		}
	}
}
