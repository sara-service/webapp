package bwfdm.sara.transfer.rewrite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RewriteTest {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private File root;
	private Git git;
	private Repository repo;
	private RewriteCache cache;
	private Map<String, ObjectId> objects;
	private ObjectId a1, a2, a3, a4, a5, a6, a7, a8, a9, a10;
	private ObjectId b1, b2, b3, b4, b5, b6, b7, b8;
	private ObjectId c1, c2, c3, c4, c5, c6, c7, c8, c9;
	private Ref ta4, tb3, tb5, tc3, tc6, a, b, c;

	@Before
	public void createTestRepo() throws IOException, GitAPIException {
		root = Files.createTempDirectory("rewrite-test").toFile();
		git = new InitCommand().setBare(true).setDirectory(root).call();
		repo = git.getRepository();
		objects = new HashMap<>();
		cache = new RewriteCache();

		// two interlinked chains (Ax = tagged, ax = untagged):
		// . B8--b7--b6--B5--b4--B3--b2--b1
		// . . . . \ . . . . . \ . . . . . \
		// a10--a9--a8--a7--a6--a5--A4--a3--a2--a1
		a1 = commit("a1");
		a2 = commit("a2", a1);
		a3 = commit("a3", a2);
		a4 = commit("a4", a3);
		a5 = commit("a5", a4);
		a6 = commit("a6", a5);
		a7 = commit("a7", a6);
		a8 = commit("a8", a7);
		a9 = commit("a9", a8);
		a10 = commit("a10", a9);
		a = branch("a", a10);
		b1 = commit("b1", a2);
		b2 = commit("b2", b1);
		b3 = commit("b3", b2);
		b4 = commit("b4", b3, a5);
		b5 = commit("b5", b4);
		b6 = commit("b6", b5);
		b7 = commit("b7", b6, a8);
		b8 = commit("b8", b7);
		b = branch("b", b8);
		// a separate, disjoint linear chain
		c1 = commit("c1");
		c2 = commit("c2", c1);
		c3 = commit("c3", c2);
		c4 = commit("c4", c3);
		c5 = commit("c5", c4);
		c6 = commit("c6", c5);
		c7 = commit("c7", c6);
		c8 = commit("c8", c7);
		c9 = commit("c9", c8);
		c = branch("c", c9);

		// some tags to mark important points
		ta4 = tag("ta4", a4);
		tb3 = tag("tb3", b3);
		tb5 = tag("tb5", b5);
		tc3 = tag("tc3", c3);
		tc6 = tag("tc6", c6);
	}

	private ObjectId commit(final String id, final ObjectId... parents)
			throws IOException {
		try (final ObjectInserter ins = repo.newObjectInserter()) {
			final ObjectId file = ins.insert(Constants.OBJ_BLOB,
					id.getBytes(UTF8));
			final TreeFormatter tree = new TreeFormatter(1);
			tree.append("id.txt", FileMode.REGULAR_FILE, file);

			final CommitBuilder commit = new CommitBuilder();
			commit.setTreeId(ins.insert(tree));
			commit.setAuthor(new PersonIdent(id, id + "@example.org"));
			commit.setCommitter(new PersonIdent(id, id + "@example.com"));
			commit.setMessage(id);
			commit.setParentIds(parents);

			final ObjectId obj = ins.insert(commit);
			objects.put(id, obj);
			return obj;
		}
	}

	private Ref tag(final String id, final AnyObjectId commit)
			throws IOException, GitAPIException {
		return git.tag().setName(id).setObjectId(repo.parseCommit(commit))
				.call();
	}

	private Ref branch(final String id, final AnyObjectId commit)
			throws IOException, GitAPIException {
		return git.branchCreate().setName(id)
				.setStartPoint(repo.parseCommit(commit)).call();
	}

	/**
	 * A null rewrite (which keeps every commit) must not change the commit IDs.
	 */
	@Test
	public void testNullRewrite() throws IOException {
		try (RewriteStrategy filterAll = new FilteredHistory(repo, cache) {
			@Override
			protected boolean isSignificant(RevCommit commit,
					List<ObjectId> parents) {
				return true;
			}
		}) {
			filterAll.process(repo.parseCommit(a10));
			filterAll.process(repo.parseCommit(b8));
			filterAll.process(repo.parseCommit(c9));

			for (final ObjectId object : objects.values())
				assertIdentityRewrite(object);

			// check that the rewriter actually did something, by validating
			// that it doesn't just return the ObjectId object we passed in.
			// FullHistory does it that way for sake of efficiency, but here
			// we're testing the algorithm and deliberately bypass FullHistory.
			for (final ObjectId object : objects.values())
				assertNotSame(object, cache.getRewriteResult(object).get(0));
		}
	}

	private void assertIdentityRewrite(final ObjectId object) {
		final List<ObjectId> rewritten = cache.getRewriteResult(object);
		assertEquals(1, rewritten.size());
		assertEquals(object, rewritten.get(0));
	}

	@After
	public void dispose() {
		repo.close();
		repo = null;
		git.close();
		git = null;
		objects = null;
		cache = null;
		a1 = a2 = a3 = a4 = a5 = a6 = a7 = a8 = a9 = a10 = null;
		b1 = b2 = b3 = b4 = b5 = b6 = b7 = b8 = null;
		c1 = c2 = c3 = c4 = c5 = c6 = c7 = c8 = c9 = null;
		a = b = c = ta4 = tb3 = tb5 = tc3 = tc6 = null;
		root.delete();
	}
}
