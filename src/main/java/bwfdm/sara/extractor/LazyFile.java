package bwfdm.sara.extractor;

import java.nio.charset.Charset;

import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.RepoFile;

/**
 * file that gets lazily downloaded from the repo once its contents are actually
 * needed.
 */
public class LazyFile {
	private final GitProject repo;
	private final String ref;
	private String content;
	private final RepoFile file;

	public LazyFile(final GitProject repo, final String ref, final RepoFile file) {
		this.repo = repo;
		this.ref = ref;
		this.file = file;
	}

	public String getName() {
		return file.getName();
	}

	public String getContent() {
		if (content == null)
			content = loadContent();
		return content;
	}

	private String loadContent() {
		// TODO use juniversalchardet
		return new String(repo.getBlob(ref, file.getName()),
				Charset.forName("UTF-8"));
		// return "bla";
		// try {
		// final BufferedReader in = new BufferedReader(
		// new FileReader(
		// "/home/matthias/sara/forschung/metadaten/lizenz/LicenseFinder/LICENSE"));
		// final StringBuilder buffer = new StringBuilder();
		// while (true) {
		// final String line = in.readLine();
		// if (line == null)
		// break;
		// buffer.append(line).append('\n');
		// }
		// in.close();
		// return buffer.toString();
		// } catch (final IOException e) {
		// throw new RuntimeException(e);
		// }
	}
}
