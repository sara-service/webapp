package bwfdm.sara.publication;

import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
//import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;
import jersey.repackaged.com.google.common.collect.Lists;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import bwfdm.sara.publication.db.DAO;
import bwfdm.sara.publication.db.RepositoryDAO;
import bwfdm.sara.publication.PublicationRepositoryFactory;
import bwfdm.sara.publication.PublicationRepository;

/**
 * Database containing config stuff for the publication backend.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class PublicationDatabase {

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

	/**
	 * Retrieves a list of DAO entries for given table
	 * 
	 * @param tableName
	 * 		the name of the table in the database
	 * @return
	 * 		list of entries of the given table contained in the database
	 */
	@SuppressWarnings("unchecked")
	public <D extends DAO> List<D> getList(String tableName) {
		List<Map<String, Object>> mapList = db.queryForList("select * from " + tableName);
		
		List<DAO> elems = Lists.newArrayList();
		for(Map<String, Object> entryMap : mapList)
		{
			DAO elem = null;
			try {
				elem = (DAO)Class.forName("bwfdm.sara.publication.db." + tableName + "DAO").newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
			for(Entry<String, Object> entry : entryMap.entrySet())
			{
				//System.out.println(entry.getKey() + " / " + entry.getValue());
				elem.set(entry.getKey(), entry.getValue());
				elems.add(elem);
			}
		}
		return (List<D>)elems;
	}

	/**
	 * Inserts a DAO into its database table returning a valid primary key.
	 * The targeting table is determined automatically.
	 * Parameters might violate constraints in the DB tables and hence throw SQL exceptions.
	 * FIXME this needs to be extended to tables that do not use UUID as primary keys!
	 * 
	 * @param d
	 * 		The DAO to be inserted
	 * @return
	 * 		The original DAO updated with the primary key which has been created in the DB automatically
	 */
	public DAO insertInDB(DAO d) {
		// create a database entry
		List<String> fns = d.getDynamicFieldNames();
		String tableName = (String)d.get("TABLE");
		String pkey = d.getPrimaryKey().get(0);  // FIXME solve that for pkeys consisting of multiple fields
		SimpleJdbcInsert insert;
		
		Map<String, Object> values = new HashMap<String, Object>();
		
		if (fns.contains(pkey)) {
			insert = new SimpleJdbcInsert(db).withTableName(tableName).usingGeneratedKeyColumns(pkey);
		} else {
			insert = new SimpleJdbcInsert(db).withTableName(tableName);
		}
		
		for (String fn: fns) {
			if (!fn.equals(pkey))
				values.put(fn, d.get(fn));
			else
				insert.usingGeneratedKeyColumns(pkey);
		}
		
		if (fns.contains(pkey)) {
			d.set(pkey, insert.executeAndReturnKeyHolder(values).getKeys().get(pkey));
		} else {
			insert.execute(values);
		}
		
		return d;
	}
	
	/**
	 * Updates the database table entry using an existing DAO and its primary key.
	 * Parameters might violate constraints in the DB tables and hence throw SQL exceptions.
	 * FIXME this needs to be extended to tables that do not use UUID as primary keys!
	 * 
	 * @param d
	 * 		The DAO and all its values to be updated.
	 */
	public void updateInDB(DAO d) {
		final String lmStr = "date_last_modified";
		List<String> fieldNames = d.getDynamicFieldNames();
		List<String> primaryKey = d.getPrimaryKey();

		// get table name
		String tableName = d.get("TABLE").toString();
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
			fn_value = "'" + fn_value.replaceAll("'", "''") + "'";

			if (primaryKey.contains(fn)) {
				whereString += " " + fn + "=" + fn_value + ",";
			} else {
				setString += " " + fn + "=" + fn_value + ",";
			}
		}
		whereString = whereString.substring(0, whereString.length() - 1) + " ";
		setString = setString.substring(0, setString.length() - 1) + " ";

		db.update("update " + tableName + " set " + setString + " where " + whereString);
	}

	/**
	 * Updates the DAO database table entry using an existing database entry and its primary key.
	 * @param d
	 * 		The possibly out-dated DAO containing the primary key.
	 * @return
	 * 		The updated DAO representing the current state of the database
	 */
	public <D extends DAO> D updateFromDB(D d) {
		System.out.println(d.getDynamicFieldNames());

		List<String> fieldNames = d.getDynamicFieldNames();
		List<String> primaryKey = d.getPrimaryKey();

		String tableName = d.get("TABLE").toString();
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
	
	private Map<String, String> readArguments(final String table, final Object id) {
		final Map<String, String> args = new HashMap<>();
		db.query("select param, value from " + table + " where id = UUID(?)", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				args.put(rs.getString("param"), rs.getString("value"));
			}
		}, id);
		return args;
	}
	
	public PublicationRepository newPublicationRepository(RepositoryDAO r) {
		PublicationRepositoryFactory factory = new PublicationRepositoryFactory(r);
		Map<String, String> args = readArguments("Repository_Params", r.get("uuid"));
		return factory.newPubRepo(args);
	}
}