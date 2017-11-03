package bwfdm.sara.transfer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.util.FileSystemUtils;

import bwfdm.sara.project.Ref;
import bwfdm.sara.transfer.RepoFile.FileType;

public class TransferRepo {
	private final File root;
	private Repository repo;
	private boolean upToDate;
	private boolean disposed;

	public TransferRepo(final File root) {
		this.root = root;
	}

	void setRepo(final Repository repo) {
		upToDate = true;
		this.repo = repo;
	}

	public Repository getRepo() {
		return repo;
	}

	public File getRoot() {
		return root;
	}

	public void markOutdated() {
		upToDate = false;
	}

	public boolean isUpToDate() {
		return upToDate && !disposed;
	}

	public void dispose() {
		if (disposed)
			return;

		disposed = true;
		repo = null;
		new Thread("cleanup for " + root.getName()) {
			@Override
			public void run() {
				FileSystemUtils.deleteRecursively(root);
			};
		}.start();
	}

	public boolean isDisposed() {
		return disposed;
	}

	private void checkInitialized() {
		if (!isUpToDate())
			throw new IllegalStateException(
					"TransferRepo outdated or uninitialized");
	}

	private ObjectId findObject(final Ref ref, final String path)
			throws IOException {
		final ObjectId commit = repo.resolve(ref.path);
		if (commit == null)
			throw new NoSuchElementException(ref.path);
		final RevTree tree = repo.parseCommit(commit).getTree();
		if (path.isEmpty()) // we want the root
			return tree;
		final TreeWalk treewalk = TreeWalk.forPath(repo, path, tree);
		if (treewalk == null) // path not in tree
			return null;
		final ObjectId object = treewalk.getObjectId(0);
		if (object.equals(ObjectId.zeroId())) // probably impossible
			return null;
		return object;
	}

	public byte[] getBlob(final Ref ref, final String path) throws IOException {
		checkInitialized();
		final ObjectId file = findObject(ref, path);
		if (file == null)
			return null;
		return repo.open(file).getBytes();
	}

	public byte[] getBlob(final String hash) throws IOException {
		return repo.open(ObjectId.fromString(hash)).getBytes();
	}

	public List<RepoFile> getFiles(final Ref ref, final String path)
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

	public long getCommitDate(final String ref) throws IOException {
		final ObjectId id = repo.resolve(ref);
		if (id == null)
			throw new NoSuchElementException(ref);
		// FIXME 32-bit timestamp in an API! in 2017!!! wtf????
		return repo.parseCommit(id).getCommitTime();
	}
}
