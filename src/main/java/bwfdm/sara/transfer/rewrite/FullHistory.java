package bwfdm.sara.transfer.rewrite;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Dummy rewriter to handle the "full history" case. This doesn't actually do
 * anything except marking all commits as rewriting to themselves, which is
 * needed for other rewriters to find them when they hit them.
 */
public class FullHistory extends RewriteStrategy {
	private final Repository repo;

	public FullHistory(final Repository repo, final RewriteCache cache) {
		super(cache);
		this.repo = repo;
	}

	@Override
	public void process(final RevCommit head) throws IOException {
		final Queue<RevCommit> queue = new LinkedBlockingQueue<>();
		queue.add(head);
		while (!queue.isEmpty()) {
			final RevCommit commit = repo.parseCommit(queue.remove());
			// we don't change the commit IDs, so we don't wait for the parents
			// to be rewritten here.
			if (!cache.contains(commit))
				cache.keep(commit, commit);

			// enqueue all parents
			for (final RevCommit parent : commit.getParents())
				if (!cache.contains(parent.getId()))
					queue.add(parent);
		}
	}
}
