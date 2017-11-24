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

import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.git.ArchiveRepoFactory;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;

/**
 * Database containing config stuff for the web frontend. Effectively read-only.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class ConfigDatabase {
	private static final String LICENSES_TABLE = "fe_supported_licenses";
	private static final String GITREPOS_TABLE = "fe_gitrepos";
	private static final String GITREPO_PARAM_TABLE = "fe_gitrepo_params";
	private static final String ARCHIVES_TABLE = "fe_archives";
	private static final String ARCHIVE_PARAM_TABLE = "fe_archive_params";

	// the kind of name you only get if you use Spring:
	private static final RowMapper<GitRepoFactory> GIT_REPO_FACTORY_MAPPER = new RowMapper<GitRepoFactory>() {
		@Override
		public GitRepoFactory mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			final String id = rs.getString("id");
			final String displayName = rs.getString("display_name");
			final String adapter = rs.getString("adapter");
			return new GitRepoFactory(id, displayName, adapter);
		}
	};

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

	/** @return a list of all supported git repos */
	public List<GitRepoFactory> getGitRepos() {
		return db.query("select id, display_name, adapter from "
				+ GITREPOS_TABLE, GIT_REPO_FACTORY_MAPPER);
	}

	/**
	 * @param id
	 *            git repo name used in the {@value GITLABS_TABLE} table
	 * @return a new instance of the named {@link GitRepo}
	 */
	public GitRepo newGitRepo(final String id) {
		final GitRepoFactory factory = db.queryForObject(
				"select id, display_name, adapter from " + GITREPOS_TABLE
						+ " where id = ?", GIT_REPO_FACTORY_MAPPER, id);
		return factory.newGitRepo(readArguments(GITREPO_PARAM_TABLE, id));
	}

	/**
	 * @param id
	 *            git repo name used in the {@value #GITREPOS_TABLE} table
	 * @return a new instance of the named {@link ArchiveRepo}
	 */
	public ArchiveRepo newGitArchive(final String id) {
		final String adapter = db.queryForObject("select adapter from "
				+ ARCHIVES_TABLE + " where id = ?", String.class, id);
		final Map<String, String> args = readArguments(ARCHIVE_PARAM_TABLE, id);
		return ArchiveRepoFactory.newArchiveRepo(adapter, args);
	}

	private Map<String, String> readArguments(final String table,
			final String id) {
		final Map<String, String> args = new HashMap<>();
		db.query("select param, value from " + table + " where id = ?",
				new RowCallbackHandler() {
					@Override
					public void processRow(final ResultSet rs)
							throws SQLException {
						args.put(rs.getString("param"), rs.getString("value"));
					}
				}, id);
		return args;
	}
}
