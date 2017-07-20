package bwfdm.sara.extractor;

import java.io.IOException;

/**
 * file that gets lazily downloaded from the repo once its contents are actually
 * needed.
 */
public interface LazyFile {
	public String getName();

	public String getContent() throws IOException;
}
