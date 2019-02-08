package bwfdm.sara.transfer.rewrite;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

public abstract class RewriteStrategy implements AutoCloseable {
	protected final RewriteCache cache;

	public RewriteStrategy(final RewriteCache cache) {
		this.cache = cache;
	}

	public ObjectId rewrite(final RevCommit head) throws IOException {
		process(head);
		final List<ObjectId> results = cache.getRewriteResult(head);
		if (results.size() != 1)
			throw new IllegalStateException("history rewriting produced "
					+ results.size() + " heads (should be exactly one)");
		return results.get(0);
	}

	public abstract void process(final RevCommit head) throws IOException;

	@Override
	public void close() {
	}
}
