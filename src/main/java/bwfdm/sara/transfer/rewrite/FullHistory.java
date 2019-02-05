package bwfdm.sara.transfer.rewrite;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Dummy rewriter to handle the "full history" case. This doesn't actually do
 * anything except marking all commits as rewriting to themselves, which is
 * needed for other rewriters to find them when they hit them.
 */
public class FullHistory extends RewriteStrategy {
	public FullHistory(final Repository repo, final RewriteCache cache) {
		super(repo, cache);
	}

	@Override
	public void process(final RevCommit head) {
		final Queue<RevCommit> queue = new LinkedBlockingQueue<>();
		while (!queue.isEmpty()) {
			final RevCommit commit = queue.remove();
			// we don't change the commit IDs, so we don't wait for the parents
			// to be rewritten here.
			cache.setRewriteResult(commit, Arrays.<ObjectId> asList(commit));

			// enqueue all parents
			for (final RevCommit parent : commit.getParents())
				if (!cache.contains(parent.getId()))
					queue.add(parent);
		}
	}
}
