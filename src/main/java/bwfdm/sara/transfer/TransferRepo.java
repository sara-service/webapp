package bwfdm.sara.transfer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEditor.DeletePath;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.util.FileSystemUtils;

import bwfdm.sara.project.Ref;
import bwfdm.sara.project.Ref.RefType;
import bwfdm.sara.transfer.RepoFile.FileType;

public class TransferRepo {
	private static final Log LOGGER = LogFactory.getLog(TransferRepo.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final byte[] ROOT_PREFIX = new byte[0];
	private static final int MAX_SYMLINKS = 40; // match Linux here
	private static final MiniCharDet CHARSET_DETECTOR = new MiniCharDet();

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

	public RevCommit getCommit(final Ref ref) throws IOException {
		return repo.parseCommit(resolve(repo, Constants.R_REFS + ref.path));
	}

	public List<Ref> getTags() throws IOException {
		final List<Ref> tags = new ArrayList<>();
		for (final String name : repo.getTags().keySet())
			tags.add(new Ref(RefType.TAG, name));
		return tags;
	}

	public ObjectId getFile(final Ref ref, final String path)
			throws IOException {
		checkInitialized();
		final RevTree tree = getCommit(ref).getTree();

		String curPath = path;
		for (int symlinks = 0; symlinks < MAX_SYMLINKS; symlinks++) {
			// lookup the path
			final TreeWalk treewalk = TreeWalk.forPath(repo, curPath, tree);
			if (treewalk == null) { // path not in tree
				LOGGER.info(root + ": " + curPath + " not in tree");
				return null;
			}
			final ObjectId object = treewalk.getObjectId(0);
			if (object.equals(ObjectId.zeroId())) { // object missing?!
				LOGGER.info(root + ": no object for " + curPath);
				return null;
			}

			// check object type instead of blindly reading weird objects
			final FileMode mode = treewalk.getFileMode();
			if (mode.equals(FileMode.REGULAR_FILE)
					|| mode.equals(FileMode.EXECUTABLE_FILE))
				return object;
			if (!mode.equals(FileMode.SYMLINK)) {
				LOGGER.info(root + ": " + curPath + " has mode " + mode);
				return null;
			}

			// recurse to resolve symbolic links
			curPath = new String(repo.open(object).getBytes(), UTF8);
		}

		LOGGER.info(root + ": " + curPath + " is a symlink loop");
		return null;
	}

	public String readString(final Ref ref, final String path)
			throws IOException {
		final ObjectId file = getFile(ref, path);
		if (file == null)
			return null;
		return readString(file);
	}

	/** Used by JRuby to read files by hash. */
	public String readString(final ObjectId hash) throws IOException {
		checkInitialized();
		return CHARSET_DETECTOR.detect(repo.open(hash).getBytes());
	}

	public List<RepoFile> getFiles(final Ref ref) throws IOException {
		checkInitialized();
		final List<RepoFile> files = new ArrayList<>();
		try (final TreeWalk walk = new TreeWalk(repo)) {
			walk.addTree(getCommit(ref).getTree());
			walk.setRecursive(false);
			while (walk.next()) {
				final FileMode mode = walk.getFileMode();
				final FileType type;
				if (mode.equals(FileMode.REGULAR_FILE)
						|| mode.equals(FileMode.EXECUTABLE_FILE)
						|| mode.equals(FileMode.SYMLINK))
					type = FileType.FILE;
				else if (mode.equals(FileMode.TYPE_TREE))
					type = FileType.DIRECTORY;
				else
					continue;
				files.add(new RepoFile(walk.getNameString(),
						walk.getObjectId(0), type));
			}
		}
		return files;
	}

	public ObjectId insertBlob(final String data) throws IOException {
		try (final ObjectInserter ins = repo.newObjectInserter()) {
			return ins.insert(Constants.OBJ_BLOB, data.getBytes(UTF8));
		}
	}

	public ObjectId updateFiles(final Ref ref,
			final Map<String, ObjectId> files) throws IOException {
		// build an initial index in-memory, because bare repos don't have one
		// on disk
		final DirCache index = DirCache.newInCore();
		final DirCacheBuilder init = index.builder();
		init.addTree(ROOT_PREFIX, 0, repo.newObjectReader(),
				getCommit(ref).getTree());
		init.finish();

		// delete all files to be replaced. this doesn't do anything for files
		// that don't exist in the repo.
		final DirCacheEditor delete = index.editor();
		for (final String file : files.keySet())
			delete.add(new DeletePath(file));
		delete.finish();

		try (final ObjectInserter ins = repo.newObjectInserter()) {
			// create entries for all new files. because we deleted them
			// beforehand, this won't cause any duplicate paths.
			final DirCacheBuilder create = index.builder();
			create.keep(0, index.getEntryCount());
			for (final String file : files.keySet()) {
				final ObjectId data = files.get(file);
				if (data == null)
					continue;
				final DirCacheEntry entry = new DirCacheEntry(file);
				entry.setFileMode(FileMode.REGULAR_FILE);
				entry.setObjectId(data);
				create.add(entry);
			}
			create.finish();

			return index.writeTree(ins);
		}
	}

	public ObjectId insertCommit(final CommitBuilder commit)
			throws IOException {
		try (final ObjectInserter ins = repo.newObjectInserter()) {
			return ins.insert(commit);
		}
	}

	public static ObjectId resolve(final Repository repo, final String refPath)
			throws IOException {
		final org.eclipse.jgit.lib.Ref ref = repo.exactRef(refPath);
		if (ref == null)
			throw new NoSuchElementException("nonexistent ref " + refPath);
		final org.eclipse.jgit.lib.Ref leaf = repo.peel(ref.getLeaf());
		if (!leaf.isPeeled())
			throw new IOException("failed to peel ref " + leaf.getName());

		// annotated tag?
		final ObjectId tagCommit = leaf.getPeeledObjectId();
		if (tagCommit != null)
			return tagCommit;
		// all other refs
		final ObjectId commit = leaf.getObjectId();
		if (commit == null)
			throw new IllegalStateException("unborn ref " + refPath);
		return commit;
	}
}
