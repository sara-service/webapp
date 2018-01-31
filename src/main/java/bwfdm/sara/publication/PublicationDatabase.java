package bwfdm.sara.publication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.fasterxml.jackson.databind.ObjectMapper;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.DAOImpl;
import bwfdm.sara.publication.db.RepositoryDAO;
import bwfdm.sara.publication.db.TableName;

public class PublicationDatabase {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String GENERATED_PRIMARY_KEY = "uuid";
	private final JdbcTemplate db;
	private final DataSource ds;

	/**
	 * Creates a DAO for reading config values.
	 * 
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 */
	public PublicationDatabase(final DataSource db) {
		this.ds = db;
		this.db = new JdbcTemplate(db);
	}

	public final DataSource getDataSource() {
		return this.ds;
	}

	private Object[] getPrimaryKeyObjects(DAO d) {
		final SortedSet<String> pKey = d.getPrimaryKey();
		Object[] objs = new Object[pKey.size()];
		int i = 0;
		for (String fn : pKey) {
			objs[i] = d.get(fn);
		}
		return objs;
	}

	public Boolean exists(DAO d) {
		String tableName = getTableName(d);
		String whereString = "";

		for (String fn : d.getPrimaryKey()) {
			String fn_value;
			Object fn_obj = d.get(fn);
			if (fn_obj == null)
				fn_value = "null";
			else
				fn_value = fn_obj.toString();

			// quote all possibly contained 's
			fn_value = "'" + fn_value.replaceAll("'", "''") + "'";

			whereString += " " + fn + "=?,";
		}

		whereString = whereString.substring(0, whereString.length() - 1) + " ";
		int count = db.queryForObject("SELECT count(*) FROM " + tableName + " WHERE " + whereString,
				getPrimaryKeyObjects(d), Integer.class);

		if (count > 1) {
			System.out.println("pKey ERROR: must be unique!");
		}
		return (count == 1);
	}

	/**
	 * Retrieves a list of DAO entries for given table
	 * 
	 * @param tableName
	 *            the name of the table in the database
	 * @return list of entries of the given table contained in the database
	 */
	public <D extends DAO> List<D> getList(Class<D> cls) {
		List<Map<String, Object>> mapList = db.queryForList("select * from " + getTableName(cls));
		List<D> elems = new ArrayList<>();
		for (Map<String, Object> entryMap : mapList)
			elems.add(createDAO(cls, entryMap));
		return elems;
	}

	private <D extends DAO> D createDAO(Class<? extends D> cls,
			Map<String, Object> fields) {
		// create empty object from just the primary key columns
		Map<String, Object> pkey = new HashMap<>(fields);
		pkey.keySet().retainAll(DAOImpl.getPrimaryKey(cls));
		final D elem = MAPPER.convertValue(pkey, cls);

		// explicitly set the remaining fields from the remaining columns
		fields.keySet().removeAll(pkey.keySet());
		for (String field : DAOImpl.getDynamicFieldNames(cls))
			elem.set(field, fields.get(field));
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
		for (String field : d.getDynamicFieldNames())
			values.put(field, d.get(field));

		SimpleJdbcInsert insert = new SimpleJdbcInsert(db)
				.withTableName(getTableName(d));
		final SortedSet<String> keys = d.getPrimaryKey();
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
	 * Updates the database table entry using an existing DAO and its primary key.
	 * Parameters might violate constraints in the DB tables and hence throw SQL
	 * exceptions. Since the 'uuid' field is auto-generated by the DB it is excluded
	 * from the update.
	 * 
	 * @param d
	 *            The DAO and all its values to be updated.
	 */
	public void updateInDB(DAO d) throws DataAccessException {
		final String lmStr = "date_last_modified";
		List<String> fieldNames = d.getDynamicFieldNames();
		SortedSet<String> primaryKey = d.getPrimaryKey();
		fieldNames.addAll(primaryKey);

		// get table name
		String tableName = getTableName(d);
		String setString = "";
		String whereString = "";

		for (String fn : fieldNames) {
			String fn_value;

			// if table has a last modified field update it
			// unless it is used as pkey constraint
			if (fn.equals(lmStr) & !primaryKey.contains(lmStr)) {
				fn_value = "now()";
			} else {
				Object fnObj = d.get(fn);
				if (fnObj == null)
					fn_value = "null";
				else
					fn_value = fnObj.toString();
			}
			// quote all possibly contained 's
			// FIXME FIXME FIXME here be SQL injections FIXME FIXME FIXME
			fn_value = "'" + fn_value.replaceAll("'", "''") + "'";

			if (primaryKey.contains(fn)) {
				whereString += " " + fn + "=" + fn_value + ",";
			}
			if (!fn.equals("uuid")) {
				setString += " " + fn + "=" + fn_value + ",";
			}
		}
		whereString = whereString.substring(0, whereString.length() - 1) + " ";
		setString = setString.substring(0, setString.length() - 1) + " ";

		db.update("update " + tableName + " set " + setString + " where " + whereString);
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
	 * @return The updated DAO representing the current state of the database
	 */
	public <D extends DAO> D updateFromDB(D d) throws DataAccessException {
		System.out.println(d.getDynamicFieldNames());

		List<String> fieldNames = d.getDynamicFieldNames();
		SortedSet<String> primaryKey = d.getPrimaryKey();
		fieldNames.addAll(primaryKey);

		String tableName = getTableName(d);
		String whereString = "";

		for (String fn : fieldNames) {
			String fn_value;
			Object fn_obj = d.get(fn);
			if (fn_obj == null)
				fn_value = "null";
			else
				fn_value = fn_obj.toString();

			// quote all possibly contained 's
			fn_value = "'" + fn_value.replaceAll("'", "''") + "'";

			if (primaryKey.contains(fn)) {
				whereString += " " + fn + "=" + fn_value + ",";
			}
		}
		whereString = whereString.substring(0, whereString.length() - 1) + " ";

		Map<String, Object> singleMap = db.queryForMap("select * from " + tableName + " where " + whereString);

		for (Entry<String, Object> entry : singleMap.entrySet()) {
			System.out.println(entry.getKey() + " / " + entry.getValue());
			d.set(entry.getKey(), entry.getValue());
		}
		return d;
	}

	public Boolean deleteFromDB(DAO d) throws DataAccessException {
		String whereString = "";

		for (String fn : d.getPrimaryKey()) {
			String fn_value;
			Object fn_obj = d.get(fn);
			if (fn_obj == null)
				fn_value = "null";
			else
				fn_value = fn_obj.toString();

			// quote all possibly contained 's
			fn_value = "'" + fn_value.replaceAll("'", "''") + "'";
			whereString += " " + fn + "=" + fn_value + " and";
		}
		whereString = whereString.substring(0, whereString.length() - 4) + " ";
		if (exists(d)) {
			db.execute("delete from " + getTableName(d) + " where " + whereString);
			return true;
		} else {
			return false;
		}

	}

	public PublicationRepository newPublicationRepository(RepositoryDAO r) {
		PublicationRepositoryFactory factory = new PublicationRepositoryFactory(r);
		Map<String, Object> args = new HashMap<>();
		args.put("dao", r);
		return factory.newPublicationRepository(args);
	}
}