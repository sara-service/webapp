package bwfdm.sara.git.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.Contributor;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.git.Tag;

public class LocalProject implements GitProject {
	private final Repository repo;
	private final File dir;

	public LocalProject(final File dir) throws IOException {
		this.dir = dir;
		repo = Git.open(dir).getRepository();
	}

	@Override
	public String getProjectViewURL() {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public String getEditURL(final String branch, final String path) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public String getCreateURL(final String branch, final String path) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public List<Branch> getBranches() {
		final List<Branch> branches = new ArrayList<>();
		for (final String name : getRefs(Constants.R_HEADS))
			branches.add(new Branch(name, false, name.equals(Constants.MASTER)));
		return branches;
	}

	private Set<String> getRefs(final String prefix) {
		try {
			return repo.getRefDatabase().getRefs(prefix).keySet();
		} catch (final IOException e) {
			throw new RuntimeException("cannot access repository", e);
		}
	}

	@Override
	public List<Tag> getTags() {
		final List<Tag> tags = new ArrayList<>();
		for (final String name : getRefs(Constants.R_TAGS))
			tags.add(new Tag(name, false));
		return tags;
	}

	@Override
	public ProjectInfo getProjectInfo() {
		final File meta = repo.getDirectory();
		final String name;
		if (repo.isBare())
			name = meta.getName().replaceFirst("\\.git$", "");
		else
			name = meta.getParentFile().getName();
		final File desc = new File(meta, "description");
		return new ProjectInfo(meta.getAbsolutePath(), name, readFile(desc),
				"master");
	}

	private String readFile(final File desc) {
		if (!desc.isFile() || desc.length() == 0)
			return "";

		try (final BufferedReader in = new BufferedReader(new FileReader(desc))) {
			final StringBuilder buffer = new StringBuilder();
			while (true) {
				final String line = in.readLine();
				if (line == null)
					break;
				buffer.append(line).append(' ');
			}
			return buffer.toString();
		} catch (final IOException e) {
			throw new RuntimeException("cannot read " + desc.getAbsolutePath(),
					e);
		}
	}

	@Override
	public void enableClone(final boolean enable) {
		return;
	}

	@Override
	public String getCloneURI() {
		return dir.getAbsolutePath();
	}

	@Override
	public void setCredentials(final TransportCommand<?, ?> tx) {
		return;
	}

	@Override
	public List<Commit> getCommits(final String ref, final int limit) {
		final List<Commit> commits = new ArrayList<>();
		try {
			try (final RevWalk walk = new RevWalk(repo)) {
				walk.sort(RevSort.TOPO);
				walk.sort(RevSort.COMMIT_TIME_DESC, true);
				walk.markStart(walk.parseCommit(getCommit(ref)));
				while (true) {
					final RevCommit c = walk.next();
					if (c == null)
						break;
					commits.add(new Commit(c.getName(), c.getShortMessage(),
							new Date(c.getCommitTime() * 1000)));
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return commits;
	}

	private ObjectId getObject(final String ref, final String path)
			throws IOException {
		final ObjectId commit = getCommit(ref);
		final RevTree tree = repo.parseCommit(commit).getTree();
		if (path.isEmpty())
			return tree;
		final ObjectId object = TreeWalk.forPath(repo, path, tree).getObjectId(
				0);
		if (object.equals(ObjectId.zeroId()))
			return null;
		return object;
	}

	private ObjectId getCommit(final String ref) throws IOException {
		// final ObjectId commit = repo.exactRef(ref);
		// if (commit == null)
		// throw new NoSuchElementException(ref);
		// return commit;
		return null;
	}

	@Override
	public byte[] getBlob(final String ref, final String path) {
		try {
			final ObjectId file = getObject(ref, path);
			if (file == null)
				return null;
			return repo.open(file).getBytes();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Contributor> getContributors(final String ref) {
		final List<Contributor> contributors = new ArrayList<>();
		try {
			try (final RevWalk walk = new RevWalk(repo)) {
				walk.sort(RevSort.TOPO);
				walk.sort(RevSort.COMMIT_TIME_DESC, true);
				walk.markStart(walk.parseCommit(getCommit(ref)));
				while (true) {
					final RevCommit c = walk.next();
					if (c == null)
						break;
					final PersonIdent id = c.getAuthorIdent();
					// FIXME only once!
					contributors.add(new Contributor(id.getName(), id
							.getEmailAddress(), 1));
				}
			}
			return contributors;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
