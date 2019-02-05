package bwfdm.sara.transfer.rewrite;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public abstract class RewriteStrategy {
	protected final Repository repo;
	protected final RewriteCache cache;

	public RewriteStrategy(final Repository repo, final RewriteCache cache) {
		this.repo = repo;
		this.cache = cache;
	}

	public abstract void process(final RevCommit head) throws IOException;
}
