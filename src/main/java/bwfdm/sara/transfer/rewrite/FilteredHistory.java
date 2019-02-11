package bwfdm.sara.transfer.rewrite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Generic history rewriting implementation. This can support basically any
 * notion of "important" commits, as long as they can be somehow identified from
 * the commit itself.
 * <p>
 * This actually handles "full history" correctly when
 * {@link #isSignificant(RevCommit, List)} always returns <code>true</code>, but
 * it's hopelessly inefficient at that.
 */
public abstract class FilteredHistory extends RewriteStrategy
		implements AutoCloseable {
	private final ObjectInserter ins;
	private final Repository repo;

	public FilteredHistory(final Repository repo, final RewriteCache cache) {
		super(cache);
		this.repo = repo;
		ins = repo.newObjectInserter();
	}

	/**
	 * Algorithm:
	 * 
	 * <pre>
	 * rewrite() {<br>
	 *   parents = the empty set<br>
	 *   for each parent:<br>
	 *     parents += rewrite(parent)<br>
	 *   if (significant)<br>
	 *     return new merge commit(parents)<br>
	 *   else<br>
	 *     return parents;<br>
	 * }
	 * </pre>
	 * 
	 * The actual implementation isn't recursive, but uses an explicit stack.
	 * Java's stack just isn't as deep as Git's commit chains are long...
	 */
	@Override
	public void process(final RevCommit root) throws IOException {
		Stack head = new Stack(null, root);
		while (head != null) {
			if (head.nextParent >= head.commit.getParentCount()) {
				// we now know what all our parents will rewrite to, so we can
				// start rewriting the current head.
				final List<ObjectId> parents;
				if (head.commit.getParentCount() != 1) {
					// this is a merge commit, so compute the union of what all
					// our parents rewrite to. needs to preserve the order of
					// commits.
					// done the O(n²) way because merges generally have 2
					// parents (they can have more, but almost never do), and
					// using a LinkedHashMaps is more expensive than O(2²).
					parents = new ArrayList<>();
					for (final RevCommit p : head.commit.getParents())
						for (final ObjectId q : cache.getRewriteResult(p))
							if (!parents.contains(q))
								parents.add(q);
				} else
					// when there is exactly one parent, we just inherit its
					// parents. for the common case of a long chain of commits,
					// this avoids generating an extra ArrayList instance for
					// every single commit.
					parents = cache.getRewriteResult(head.commit.getParent(0));

				if (isSignificant(head.commit, parents)) {
					// significant commits rewrite to a single commit with all
					// rewritten parents
					final ObjectId rewritten = ins
							.insert(rewrite(head.commit, parents));
					cache.keep(head.commit, rewritten);
				} else
					// insignificant commits rewrite to the set of their parents
					cache.omit(head.commit, parents);

				head = head.prev;
			} else {
				// recurse into all parents that haven't yet been rewritten
				final int curParent = head.nextParent++;
				final RevCommit parent = head.commit.getParent(curParent);
				if (!cache.contains(parent))
					head = new Stack(head, repo.parseCommit(parent));
			}
		}
	}

	/** Mini linked-list stack. */
	private class Stack {
		private final Stack prev;
		private final RevCommit commit;
		private int nextParent;

		public Stack(final Stack prev, final RevCommit commit) {
			this.prev = prev;
			this.commit = commit;
			this.nextParent = 0;
		}
	}

	@Override
	public void close() {
		ins.close();
	}

	protected abstract boolean isSignificant(final RevCommit commit,
			final List<ObjectId> parents);

	private CommitBuilder rewrite(final RevCommit orig,
			final List<ObjectId> parents) throws IOException {
		final CommitBuilder rewritten = new CommitBuilder();
		rewritten.setTreeId(orig.getTree());
		rewritten.setParentIds(parents);
		// this loses the author and committer of the removed commits. so does
		// squashing history, though.
		rewritten.setAuthor(orig.getAuthorIdent());
		rewritten.setCommitter(orig.getCommitterIdent());
		// this also loses the messages of removed commits. squashing
		// concatenates them and lets the user edit the mess into something
		// useful. we don't have the user here, and (s)he probably doesn't want
		// to see all the messy commit messages preserved anyway.
		rewritten.setMessage(orig.getFullMessage());
		rewritten.setEncoding(orig.getEncoding());
		return rewritten;
	}
}
