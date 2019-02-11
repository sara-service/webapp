package bwfdm.sara.transfer.rewrite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.TagCommand;
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

import bwfdm.sara.project.RefAction.PublicationMethod;

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
		ta4 = tag("ta4", a4, true);
		tb3 = tag("tb3", b3, false);
		tb5 = tag("tb5", b5, true);
		tc3 = tag("tc3", c3, false);
		tc6 = tag("tc6", c6, true);
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

	private Ref tag(final String id, final AnyObjectId commit,
			final boolean annotated) throws IOException, GitAPIException {
		final TagCommand tagger = git.tag().setName(id)
				.setObjectId(repo.parseCommit(commit)).setAnnotated(annotated);
		if (annotated) {
			tagger.setMessage("tag " + id);
			tagger.setTagger(new PersonIdent(id, id + "@example.gov"));
		}
		return tagger.call();
	}

	private Ref branch(final String id, final AnyObjectId commit)
			throws IOException, GitAPIException {
		return git.branchCreate().setName(id)
				.setStartPoint(repo.parseCommit(commit)).call();
	}

	/**
	 * A null rewrite (which keeps every commit) must not change the commit IDs.
	 * This bypasses {@link HistoryRewriter} because it needs to test the
	 * non-optimized case.
	 */
	@Test
	public void testIdentityRewrite() throws IOException {
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

			// all object IDs have stayed the same.
			// if this ever fails, probably new commit headers have been added
			// and must be copied in FilteredHistory.rewrite()...
			for (final ObjectId object : objects.values()) {
				final List<ObjectId> rewritten = cache.getRewriteResult(object);
				assertEquals(1, rewritten.size());
				assertEquals(object, rewritten.get(0));
				assertTrue(cache.isKeep(object));
			}

			// check that the rewriter actually did something, by validating
			// that it doesn't just return the ObjectId object we passed in.
			// FullHistory does it that way for sake of efficiency, but here
			// we're testing the algorithm and deliberately bypass FullHistory.
			for (final ObjectId object : objects.values())
				assertNotSame(object, cache.getRewrittenCommit(object));
		}
	}

	@Test
	public void testFullHistory() throws IOException {
		final HistoryRewriter rewrite = new HistoryRewriter(repo);
		rewrite.addHead(c.getName(), PublicationMethod.FULL);
		rewrite.execute(null);
		// full rewrite must have left the object ID of the root, and all tags
		// alongside, completely unchanged.
		assertUnchangedRef(rewrite, c);
		assertUnchangedRef(rewrite, tc6);
		assertUnchangedRef(rewrite, tc3);
	}

	@Test
	public void testAbbreviatedHistory() throws IOException {
		final HistoryRewriter rewrite = new HistoryRewriter(repo);
		rewrite.addHead(c.getName(), PublicationMethod.ABBREV);
		rewrite.execute(null);
		assertFalse(rewrite.isUnchanged(c.getName()));

		// tree structure retained
		final RevCommit newC = getNew(rewrite, c);
		final RevCommit newTC3 = getNew(rewrite, tc3);
		final RevCommit newTC6 = getNew(rewrite, tc6);
		assertParents(newC, newTC6);
		assertParents(newTC6, newTC3);
		assertParents(newTC3);
		// commit metadata stays the same
		assertSameMetadata(c, newC);
		assertSameMetadata(tc6, newTC6);
		assertSameMetadata(tc3, newTC3);
	}

	@Test
	public void testLatestVersion() throws IOException {
		final HistoryRewriter rewrite = new HistoryRewriter(repo);
		rewrite.addHead(c.getName(), PublicationMethod.LATEST);
		rewrite.execute(null);
		assertFalse(rewrite.isUnchanged(c.getName()));

		// only one commit is left
		assertParents(getNew(rewrite, c));
		// commit metadata stays the same
		assertSameMetadata(c, getNew(rewrite, c));
		// tags along history are removed
		assertNull(getNew(rewrite, tc3));
		assertNull(getNew(rewrite, tc6));
	}

	private void assertParents(final RevCommit newRev,
			final RevCommit... parents) {
		assertEquals(parents.length, newRev.getParentCount());
		for (int i = 0; i < parents.length; i++)
			assertEquals(parents[i], newRev.getParent(i));
	}

	private void assertSameMetadata(final Ref oldCommit,
			final RevCommit newCommit) throws IOException {
		assertSameMetadata(resolve(oldCommit), newCommit);
	}

	private void assertSameMetadata(final RevCommit oldCommit,
			final RevCommit newCommit) throws IOException {
		assertEquals(oldCommit.getTree(), newCommit.getTree());
		assertEquals(oldCommit.getFullMessage(), newCommit.getFullMessage());
		assertEquals(oldCommit.getCommitterIdent(),
				newCommit.getCommitterIdent());
		assertEquals(oldCommit.getAuthorIdent(), newCommit.getAuthorIdent());
		assertEquals(oldCommit.getCommitTime(), newCommit.getCommitTime());
	}

	private void assertUnchangedRef(final HistoryRewriter rewrite,
			final Ref ref) throws IOException {
		assertTrue(rewrite.isUnchanged(ref.getName()));
		assertEquals(resolve(ref), getNew(rewrite, ref));
	}

	private RevCommit resolve(final Ref ref) throws IOException {
		final Ref leaf = repo.peel(ref.getLeaf());
		final ObjectId tagCommit = leaf.getPeeledObjectId();
		final ObjectId id = tagCommit != null ? tagCommit : leaf.getObjectId();
		return repo.parseCommit(id);
	}

	private RevCommit getNew(final HistoryRewriter rewrite, Ref ref)
			throws IOException {
		return rewrite.getRewrittenCommit(ref.getName());
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
