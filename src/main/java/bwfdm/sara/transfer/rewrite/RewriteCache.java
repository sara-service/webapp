package bwfdm.sara.transfer.rewrite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;

public class RewriteCache {
	private final Map<ObjectId, List<ObjectId>> rewrite = new HashMap<>();
	private final Set<ObjectId> keep = new HashSet<>();

	public void omit(final ObjectId before, final List<ObjectId> after) {
		if (rewrite.containsKey(before))
			throw new IllegalArgumentException(before + " processed twice");
		rewrite.put(before, after);
	}

	public void keep(final ObjectId before, final ObjectId after) {
		if (rewrite.containsKey(before))
			throw new IllegalArgumentException(before + " processed twice");
		rewrite.put(before, Arrays.asList(after));
		keep.add(before);
	}

	List<ObjectId> getRewriteResult(final ObjectId before) {
		return rewrite.get(before);
	}

	boolean isKeep(final ObjectId before) {
		return keep.contains(before);
	}

	public ObjectId getRewrittenCommit(final ObjectId before) {
		if (!keep.contains(before))
			return null;
		return rewrite.get(before).get(0);
	}

	public boolean contains(final ObjectId before) {
		return rewrite.containsKey(before);
	}
}
