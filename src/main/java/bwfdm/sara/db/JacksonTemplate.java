package bwfdm.sara.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper class to automatically create objects from database queries.
 * <ul>
 * <li>{@link #querySingleToObject(String, Class, Object...)} returns a single
 * object, ie. the query must return exactly one row.
 * <li>{@link #queryToObject(String, Class, Object...)} does the same, but
 * returns <code>null</code> if no rows match.
 * <li>{@link #queryToList(String, Class, Object...)} returns a list of objects,
 * hence the query can return any number of rows.
 * <li>{@link #queryToMap(String, String, Class, Class, Object...)} returns a
 * map instead and needs to know the name of the column to use for the key.
 */
public class JacksonTemplate extends JdbcTemplate {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public JacksonTemplate(final DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Returns a single database row as an object.If nothing matches, throws an
	 * exception.
	 * 
	 * @param query
	 *            SQL query returning exactly one row
	 * @param type
	 *            {@link Class} of return type, for type conversion
	 * @param args
	 *            substituted for the parameters in {@code query}
	 * @return an object constructed from the returned columns
	 */
	public <T> T querySingleToObject(final String query, final Class<T> type,
			final Object... args) {
		return convertRow(queryForMap(query, args), type);
	}

	/**
	 * Returns a single database row as an object. If nothing matches, returns
	 * <code>null</code>.
	 * 
	 * @param query
	 *            SQL query returning exactly one row
	 * @param type
	 *            {@link Class} of return type, for type conversion
	 * @param args
	 *            substituted for the parameters in {@code query}
	 * @return an object constructed from the returned columns
	 */
	public <T> T queryToObject(final String query, final Class<T> type,
			final Object... args) {
		final List<Map<String, Object>> rows = queryForList(query, args);
		if (rows.isEmpty())
			return null;
		if (rows.size() == 1)
			return convertRow(rows.get(0), type);
		throw new IllegalStateException(
				"query returned more than one row: " + query);
	}

	/**
	 * Returns a set of database rows as a list of objects.
	 * 
	 * @param query
	 *            SQL query
	 * @param typecreateObjectFromRow
	 *            {@link Class} of return type, for type conversion
	 * @param args
	 *            substituted for the parameters in {@code query}
	 * @return a list of objects constructed from the returned columns
	 */
	public <T> List<T> queryToList(final String query, final Class<T> type,
			final Object... args) {
		final List<T> list = new ArrayList<>();
		for (final Map<String, Object> row : queryForList(query, args))
			list.add(convertRow(row, type));
		return list;
	}

	/**
	 * Returns a set of database rows as a (key → value) mapping with a
	 * specified column as the key.
	 * 
	 * @param query
	 *            SQL query
	 * @param keyField
	 *            name of the field containing the <i>key</i> of the mapping
	 *            (this field will not be passed to the object's constructor)
	 * @param keyType
	 *            {@link Class} of <i>key</i> type, for type conversion
	 * @param valueType
	 *            {@link Class} of <i>value</i> type, for type conversion
	 * @param args
	 *            substituted for the parameters in {@code query}
	 * @return a map of objects constructed from the returned columns
	 */
	public <K, V> Map<K, V> queryToMap(final String query,
			final String keyField, final Class<K> keyType,
			final Class<V> valueType, final Object... args) {
		final Map<K, V> map = new HashMap<>();
		for (final Map<String, Object> row : queryForList(query, args)) {
			final K key = MAPPER.convertValue(row.remove(keyField), keyType);
			final V value = convertRow(row, valueType);
			map.put(key, value);
		}
		return map;
	}

	private static <V> V convertRow(final Map<String, Object> row,
			final Class<V> valueType) {
		final Object object;
		if (row.size() == 1)
			// if just a single column, pass that to Jackson as a string.
			// Jackson will then pass it to a single-string-argument
			// constructor. (it doesn't voluntarily do that when passed a
			// single-element map.)
			object = row.values().iterator().next();
		else
			object = row;
		return MAPPER.convertValue(object, valueType);
	}
}
