package bwfdm.sara.transfer.rewrite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;

public class RewriteCache {
	private final Map<ObjectId, List<ObjectId>> rewrite = new HashMap<>();

	public void setRewriteResult(final ObjectId before,
			final List<ObjectId> after) {
		if (rewrite.containsKey(before)) {
			final List<ObjectId> prevResult = rewrite.get(before);
			if (!prevResult.equals(after))
				throw new IllegalArgumentException(before + " rewritter to "
						+ after + " but already stored as " + prevResult);
			return;
		}
		rewrite.put(before, after);
	}

	public List<ObjectId> getRewriteResult(final ObjectId before) {
		return rewrite.get(before);
	}

	public boolean contains(final ObjectId before) {
		return rewrite.containsKey(before);
	}
}
