package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.List;
import java.util.SortedSet;

public interface DAO {
	void set(String fieldName, Object value);

	Object get(String fieldName);

	public List<String> getDynamicFieldNames();

	public SortedSet<String> getPrimaryKey();

	void dump();
}