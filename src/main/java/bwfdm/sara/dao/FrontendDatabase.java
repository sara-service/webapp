package bwfdm.sara.dao;

import java.util.Map;

import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.MetadataValue;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;

/** Interface for the database used by the web frontend. */
public interface FrontendDatabase {
	public void setProjectPath(String project);

	void loadMetadata(Map<MetadataField, MetadataValue> meta);

	public void setMetadata(final MetadataField field, final String value,
			final boolean auto);

	public void setRefAction(final Ref ref, final PublicationMethod method,
			final String firstCommit);

	public Map<Ref, RefAction> getRefActions();
}
