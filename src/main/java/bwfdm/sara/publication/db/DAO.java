package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.List;

public interface DAO {
	void set(String fieldName, Object value);

	Object get(String fieldName);

	public List<String> getDynamicFieldNames();

	// FIXME this should be a set not a list!!!
	public List<String> getPrimaryKey();

	void dump();
}