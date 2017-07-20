package bwfdm.sara.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import bwfdm.sara.extractor.RepoFile.FileType;

public class LocalRepo {
	private final Repository repo;

	public LocalRepo(final Repository repo) {
		this.repo = repo;
	}

	private ObjectId findObject(final String ref, final String path)
			throws IOException {
		final ObjectId commit = repo.resolve(ref);
		if (commit == null)
			throw new NoSuchElementException(ref);
		final RevTree tree = repo.parseCommit(commit).getTree();
		if (path.isEmpty())
			return tree;
		final ObjectId object = TreeWalk.forPath(repo, path, tree).getObjectId(
				0);
		if (object.equals(ObjectId.zeroId()))
			return null;
		return object;
	}

	public byte[] getBlob(final String ref, final String path)
			throws IOException {
		final ObjectId file = findObject(ref, path);
		if (file == null)
			return null;
		return repo.open(file).getBytes();
	}

	public byte[] getBlob(final String hash) throws IOException {
		return repo.open(ObjectId.fromString(hash)).getBytes();
	}

	public List<RepoFile> getFiles(final String ref, final String path)
			throws IOException {
		final List<RepoFile> files = new ArrayList<>();
		final ObjectId dir = findObject(ref, path);
		try (final TreeWalk walk = new TreeWalk(repo)) {
			walk.addTree(dir);
			walk.setRecursive(false);
			while (walk.next()) {
				final FileType type = walk.isSubtree() ? FileType.DIRECTORY
						: FileType.FILE;
				final String hash = walk.getObjectId(0).getName();
				files.add(new RepoFile(walk.getNameString(), hash, type));
			}
		}
		return files;
	}

	public Collection<String> getRefs() throws IOException {
		return repo.getRefDatabase().getRefs(Constants.R_REFS).keySet();
	}
}
