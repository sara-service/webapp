package bwfdm.sara.publication.db;

import java.util.List;

public interface DAO {
	void set(String fieldName, Object value);
	Object get(String fieldName);
	public List<String> getDynamicFieldNames();
	public List<String> getPrimaryKey();
	void dump();
}