package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Database containing config stuff for the web frontend. Effectively read-only.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class ConfigDatabase {
	private static final String LICENSES_TABLE = "fe_supported_licenses";

	private final JdbcTemplate db;

	/**
	 * Creates a DAO for reading config values.
	 * 
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 */
	public ConfigDatabase(final DataSource db) {
		this.db = new JdbcTemplate(db);
	}

	/**
	 * @return a list of all licenses defined in the config, in order of
	 *         preference
	 */
	public List<License> getLicenses() {
		final List<License> licenses = new ArrayList<>();
		db.query("select id, display_name, info_url from " + LICENSES_TABLE
				+ " where not hidden order by preference asc, id asc",
				new RowCallbackHandler() {
					@Override
					public void processRow(final ResultSet rs)
							throws SQLException {
						final String id = rs.getString("id");
						final String displayName = rs.getString("display_name");
						final String infoURL = rs.getString("info_url");
						licenses.add(new License(id, displayName, infoURL));
					}
				});
		return licenses;
	}

	public String getLicenseText(final String id) {
		return db.queryForObject("select full_text from " + LICENSES_TABLE
				+ " where id = ?", String.class, id);
	}
}
