package bwfdm.sara.publication.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.fasterxml.jackson.databind.ObjectMapper;

import bwfdm.sara.db.JacksonTemplate;
import bwfdm.sara.publication.Item;
import bwfdm.sara.publication.PublicationMetadatum;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.PublicationRepositoryFactory;
import bwfdm.sara.publication.Repository;

public class PublicationDatabase {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String GENERATED_PRIMARY_KEY = "uuid";
	private static final String LAST_MODIFIED_COLUMN = "date_last_modified";
	private final JacksonTemplate db;
	private final DataSource ds;

	/**
	 * Creates a DAO for reading config values.
	 * 
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 */
	public PublicationDatabase(final DataSource db) {
		this.ds = db;
		this.db = new JacksonTemplate(db);
	}

	public final DataSource getDataSource() {
		return this.ds;
	}

	public Boolean exists(DAO d) {
		final WhereClause<DAO> where = new WhereClause<DAO>(d.getClass());
		int count = db.queryForObject(
				"SELECT count(*) FROM " + getTableName(d) + where.getClause(),
				where.getParams(d), Integer.class);

		if (count > 1)
			throw new IllegalStateException("pKey ERROR: must be unique!");
		return (count == 1);
	}

	/**
	 * Retrieves a list of all DAO entries for given table
	 * 
	 * @param cls
	 *            Class of the DAO, which must be annotated with
	 *            {@link TableName} to give the name of the database table
	 * @return list of entries of the given table contained in the database
	 */
	public <D extends DAO> List<D> getList(Class<D> cls) {
		return getList(cls, "");
	}
	
	/**
	 * Retrieves a list of all DAO entries matching a given where clause
	 * 
	 * @param cls
	 *            Class of the DAO, which must be annotated with
	 *            {@link TableName} to give the name of the database table
	 * @param where
	 *            the full WHERE clause, including the {@code WHERE} keyword (so
	 *            it can be empty if there is no WHERE clause)
	 * @param args
	 *            parameters substituted for ? in the where clause
	 * @return list of entries of the given table contained in the database
	 */
	public <D extends DAO> List<D> getList(Class<D> cls, String where,
			Object... args) {
		List<Map<String, Object>> mapList = db.queryForList(
				"select * from " + getTableName(cls) + " " + where, args);
		List<D> elems = new ArrayList<>();
		for (Map<String, Object> entryMap : mapList)
			elems.add(createDAO(cls, entryMap));
		return elems;
	}

	private <D extends DAO> D createDAO(Class<? extends D> cls,
			Map<String, Object> fields) {
		// create empty object from just the primary key columns
		Map<String, Object> pkey = new HashMap<>(fields);
		pkey.keySet().retainAll(PublicationDatabase.getPrimaryKey(cls));
		final D elem = MAPPER.convertValue(pkey, cls);

		// explicitly set the remaining fields from the remaining columns
		fields.keySet().removeAll(pkey.keySet());
		for (String field : PublicationDatabase.getDynamicFieldNames(cls))
			setField(elem, field, fields.get(field));
		return elem;
	}

	/**
	 * Inserts a DAO into its database table returning a valid primary key. The
	 * targeting table is determined automatically. Parameters might violate
	 * constraints in the DB tables and hence throw SQL exceptions.
	 * 
	 * @param d
	 *            The DAO to be inserted
	 * @return The original DAO updated with the 'uuid' key which has been created
	 *         in the DB automatically. If the table uses a 'uuid'.
	 */
	public <D extends DAO> D insertInDB(D d) throws DataAccessException {
		Map<String, Object> values = new HashMap<String, Object>();
		List<String> fields = getDynamicFieldNames(d.getClass());
		fields.addAll(getPrimaryKey(d.getClass()));
		for (String field : fields)
			values.put(field, getField(d, field));

		SimpleJdbcInsert insert = new SimpleJdbcInsert(db)
				.withTableName(getTableName(d));
		final SortedSet<String> keys = getPrimaryKey(d.getClass());
		if (keys.contains(GENERATED_PRIMARY_KEY)) {
			if (keys.size() != 1)
				throw new IllegalArgumentException(GENERATED_PRIMARY_KEY
						+ " should be the only primary key for "
						+ insert.getTableName() + ": " + keys);
			if (values.get(GENERATED_PRIMARY_KEY) != null)
				throw new IllegalArgumentException(
						"generated primary key " + GENERATED_PRIMARY_KEY
								+ " must be null for insert into "
								+ insert.getTableName() + ": "
								+ values.get(GENERATED_PRIMARY_KEY));
			values.putAll(insert.usingGeneratedKeyColumns(GENERATED_PRIMARY_KEY)
					.executeAndReturnKeyHolder(values).getKeys());
		} else
			insert.execute(values);

		// TODO determine why we need that cast here... we shouldn't.
		@SuppressWarnings("unchecked")
		Class<? extends D> cls = (Class<? extends D>) d.getClass();
		return createDAO(cls, values);
	}

	/**
	 * Updates the database table entry using an existing DAO and its primary
	 * key. Parameters might violate constraints in the DB tables and hence
	 * throw SQL exceptions. Since the primary key fields are final, they are
	 * excluded from the update.
	 * 
	 * @param d
	 *            The DAO and all its values to be updated.
	 */
	public void updateInDB(DAO d) throws DataAccessException {
		final StringBuilder set = new StringBuilder();

		final List<String> fieldNames = getDynamicFieldNames(d.getClass());
		if (fieldNames.remove(LAST_MODIFIED_COLUMN)) {
			// if table has a last modified field update it to current server
			// time
			// (we know it isn't used as pkey constraint because these aren't in
			// getDynamicFields)
			set.append(',').append(LAST_MODIFIED_COLUMN).append("=now()");
		}
		
		// build the set clause of the prepared statement, and collect the
		// corresponding values in the right order
		final Object[] values = new Object[fieldNames.size()];
		int i = 0;
		for (String fn : fieldNames) {
			set.append(',').append(fn).append("=?");
			values[i++] = getField(d, fn);
		}

		final WhereClause<DAO> where = new WhereClause<>(d.getClass());
		db.update("update " + getTableName(d) + " set " + set.substring(1)
				+ where.getClause(), where.appendParams(values, d));
	}

	/**
	 * "Upserts" the DAO into the database, ie. inserts it replacing any
	 * previous rows with the same key.
	 * 
	 * @param d
	 *            The DAO and all its values to be updated.
	 */
	public void upsertInDB(DAO d) throws DataAccessException {
		// FIXME should probably perform a proper upsert, with atomicity etc.
		deleteFromDB(d);
		insertInDB(d);
	}

	private String getTableName(Class<?> cls) {
		return cls.getAnnotation(TableName.class).value();
	}

	private <D extends DAO> String getTableName(D dao) {
		return getTableName(dao.getClass());
	}

	/**
	 * Updates the DAO using an existing database entry and its primary key.
	 * 
	 * @param d
	 *            The possibly out-dated DAO containing the primary key.
	 * 
	 * @return An entirely new DAO representing the current state of the
	 *         database
	 */
	public <D extends DAO> D updateFromDB(D d) throws DataAccessException {
 		@SuppressWarnings("unchecked")
		final Class<? extends D> cls = (Class<? extends D>) d.getClass();
		final WhereClause<D> where = new WhereClause<D>(cls);
		final Map<String, Object> singleMap = db.queryForMap(
				"select * from " + getTableName(d) + where.getClause(),
				where.getParams(d));
		return createDAO(cls, singleMap);
	}

	public boolean deleteFromDB(DAO d) throws DataAccessException {
		final WhereClause<DAO> where = new WhereClause<DAO>(
				(Class<? extends DAO>) d.getClass());
		return db.update("delete from " + getTableName(d) + where.getClause(),
				where.getParams(d)) > 0;
	}

	public PublicationRepository newPublicationRepository(Repository r) {
		PublicationRepositoryFactory factory = new PublicationRepositoryFactory(r);
		Map<String, Object> args = readArguments(getTableName(r)+ "_params", r.uuid.toString());
		args.put("dao", r);
		return factory.newPublicationRepository(args);
	}
	
	/** @return a list of all supported publication repositories */
	
	public List<PublicationRepository> getPubRepos() {
		// FIXME why does this convert a list into a... list??
		List<PublicationRepository> repos = new ArrayList<PublicationRepository>();
		for (final Repository r: getList(Repository.class)) {
			repos.add(newPublicationRepository(r));
		}
		return repos;
	}
    
	public Map<PublicationField, String> getMetadata(final UUID item) {
		List<PublicationMetadatum> data = getList(PublicationMetadatum.class,
				"where item_uuid = ?", item);
		Map<PublicationField, String> map = new HashMap<PublicationField, String>(
				data.size());
		for (PublicationMetadatum datum : data)
			map.put(PublicationField.forDisplayName(datum.field), datum.value);
		return map;
	}

	public void setMetadata(final UUID item,
			final Map<PublicationField, String> values) {
		for (PublicationField field : values.keySet()) {
			final PublicationMetadatum row = new PublicationMetadatum(
					item, field.getDisplayName());
			row.value = values.get(field);
			upsertInDB(row);
		}
	}

	public List<Item> getPublishedItems(final UUID sourceUUID,
			final String userID) {
		return getList(Item.class,
				"where source_uuid = ? and source_user_id = ?", sourceUUID,
				userID);
	}

	public static SortedSet<String> getPrimaryKey(Class<? extends DAO> cls) {
		final SortedSet<String> fields = new TreeSet<>();
		for (final Field f : cls.getFields())
			if (f.isAnnotationPresent(PrimaryKey.class))
				fields.add(f.getName());
		return fields;
	}

	public static List<String> getDynamicFieldNames(Class<? extends DAO> cls) {
		final List<String> fields = new ArrayList<>();
		for (final Field f : cls.getFields())
			if (f.isAnnotationPresent(DatabaseField.class))
				fields.add(f.getName());
		return fields;
	}

	public static void setField(DAO d, String fieldName, Object value) {
		try {
			Field field = d.getClass().getDeclaredField(fieldName);
			// TODO re-enable if there are any access problems
			// field.setAccessible(true);
			field.set(d, value);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	public static Object getField(DAO d, String fieldName) {
		try {
			Field field = d.getClass().getDeclaredField(fieldName);
			return field.get(d);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Map<String, Object> readArguments(final String table,
			final String id) {
		return db.queryRowToMap(
				"select param, value from " + table + " where id = UUID(?)",
				"param", String.class, Object.class, id);
	}
}