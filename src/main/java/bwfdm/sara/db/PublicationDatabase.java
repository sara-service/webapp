package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import bwfdm.sara.publication.db.RepositoryDAO;

/**
 * Database containing config stuff for the publication backend.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class PublicationDatabase {
	private static final String ITEM_TABLE = "public.item";
	private static final String REPOSITORY_TABLE = "public.repository";

	private static final RowMapper<RepositoryDAO> REPOSITORY_MAPPER = new RowMapper<RepositoryDAO>() {
		@Override
		public RepositoryDAO mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			final String name = rs.getString("name");
		    final String URL = rs.getString("URL");
		    final String query_API_endpoint = rs.getString("query_API_endpoint");
		    final String query_user = rs.getString("query_user");
		    final String query_pwd = rs.getString("query_pwd");
		    final String submit_API_endpoint = rs.getString("submit_API_endpoint");
		    final String submit_user = rs.getString("submit_user");
		    final String submit_pwd = rs.getString("submit_pwd");
		    final String contactEMail = rs.getString("contactEMail");
		    final String version = rs.getString("version");
		    final String default_collection =rs.getString("default_collection");
			return new RepositoryDAO(
					name, URL,
					query_API_endpoint, query_user, query_pwd,
					submit_API_endpoint, submit_user, submit_pwd,
					contactEMail,
					version, default_collection
					);
					
		}
	};

	private final JdbcTemplate db;

	/**
	 * Creates a DAO for reading config values.
	 * 
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 */
	public PublicationDatabase(final DataSource db) {
		this.db = new JdbcTemplate(db);
	}
}
