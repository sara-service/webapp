package bwfdm.sara.db;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.MetadataValue;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;

/** Minimal in-memory implementation of {@link FrontendDatabase}. */
public class FakeDatabase implements FrontendDatabase {
	private final Map<MetadataField, MetadataValue> metadata = new EnumMap<>(
			MetadataField.class);
	private final Map<Ref, RefAction> actions = new HashMap<>();

	@Override
	public void setProjectPath(final String project) {
		// these are no longer valid now
		metadata.clear();
		actions.clear();
	}

	@Override
	public void loadMetadata(final Map<MetadataField, MetadataValue> meta) {
		meta.putAll(metadata);
	}

	@Override
	public void setMetadata(final MetadataField field, final String value,
			final boolean auto) {
		metadata.put(field, new MetadataValue(value, auto));
	}

	@Override
	public void loadRefActions(final Map<Ref, RefAction> actions) {
		actions.putAll(this.actions);
	}

	@Override
	public void setRefAction(final Ref ref, final PublicationMethod method,
			final String firstCommit) {
		if (method != null)
			actions.put(ref, new RefAction(ref, method, firstCommit));
		else
			actions.remove(ref);
	}
}
