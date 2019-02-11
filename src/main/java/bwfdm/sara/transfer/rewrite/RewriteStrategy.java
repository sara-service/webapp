package bwfdm.sara.transfer.rewrite;

import java.io.IOException;

import org.eclipse.jgit.revwalk.RevCommit;

public abstract class RewriteStrategy implements AutoCloseable {
	protected final RewriteCache cache;

	public RewriteStrategy(final RewriteCache cache) {
		this.cache = cache;
	}

	public abstract void process(final RevCommit head) throws IOException;

	@Override
	public void close() {
	}
}
