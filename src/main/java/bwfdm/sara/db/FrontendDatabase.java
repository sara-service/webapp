package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import bwfdm.sara.extractor.LicenseFile;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.MetadataValue;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;
import bwfdm.sara.transfer.MetadataSink;

/**
 * Database for the web frontend, storing the user's selections as they come in
 * over the REST API.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class FrontendDatabase implements MetadataSink {
	private static final String LICENSES_TABLE = "supported_licenses";
	private static final String ACTION_TABLE = "frontend_actions";
	private static final String METADATA_TABLE = "frontend_metadata";

	private static final Map<MetadataField, MetadataValue> EMPTY_METADATA;
	static {
		// create an empty dummy entry for each metadata field. this ensures
		// that all fields exist, even when they're empty.
		final Map<MetadataField, MetadataValue> map = new EnumMap<>(
				MetadataField.class);
		for (final MetadataField f : MetadataField.values())
			map.put(f, new MetadataValue(null, null));
		EMPTY_METADATA = Collections.unmodifiableMap(map);
	}

	private static final RowMapper<MetadataValue> metadataMapper = new RowMapper<MetadataValue>() {
		@Override
		public MetadataValue mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			return new MetadataValue(rs.getString("value"),
					rs.getString("autodetected"));
		}
	};
	private final String gitRepo;
	private final String project;
	private final JdbcTemplate db;
	private final TransactionTemplate transaction;

	/*
	 * TODO if settings are to be per-user instead of per-project, must add a
	 * "user" field here and to the database schema.
	 */
	/**
	 * Creates a DAO for reading / writing values for a particular project.
	 * 
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 * @param gitRepo
	 *            ID of the git repo, to qualify the project name
	 * @param project
	 *            the project name, used as database key together with gitRepo
	 */
	public FrontendDatabase(final DataSource db, final String gitRepo,
			final String project) {
		this.gitRepo = gitRepo;
		this.project = project;
		this.db = new JdbcTemplate(db);
		transaction = new TransactionTemplate(new DataSourceTransactionManager(
				db));
	}

	public List<License> getLicenses() {
		final List<License> licenses = new ArrayList<>();
		db.query("select id, display_name, info_url from " + LICENSES_TABLE
				+ " order by preference asc, id asc", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
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

	/**
	 * Get metadata. All fields will always be present (JavaScript needs this);
	 * unset values map to a {@code (null, null)} {@link MetadataValue}. The
	 * returned map is a snapshot; it doesn't reflect later changes made by
	 * {@link #setMetadata(MetadataField, String)}!
	 * 
	 * @return all metadata fields in a {@link Map}
	 */
	public Map<MetadataField, MetadataValue> getMetadata() {
		final Map<MetadataField, MetadataValue> meta = new EnumMap<>(
				EMPTY_METADATA);
		db.query("select field, value, autodetected from " + METADATA_TABLE
				+ " where repo = ? and project = ?", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final MetadataField field = MetadataField.forDisplayName(rs
						.getString("field"));
				meta.put(field, metadataMapper.mapRow(rs, 0));
			}
		}, gitRepo, project);
		return Collections.unmodifiableMap(meta);
	}

	/**
	 * Get a single metadata field.
	 * 
	 * @param field
	 *            the field to query, not {@code null} obviously
	 * @return all metadata fields in a {@link Map}
	 */
	public MetadataValue getMetadata(final MetadataField field) {
		return db.queryForObject("select value, autodetected from "
				+ METADATA_TABLE
				+ " where repo = ? and project = ? and field = ?",
				metadataMapper, gitRepo, project, field.getDisplayName());
	}

	/**
	 * Set a single metadata field.
	 * 
	 * @param field
	 *            the field to update, not {@code null}
	 * @param value
	 *            the new value, or {@code null} to revert to the autodetected
	 *            value
	 */
	public void setMetadata(final MetadataField field, final String value) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateMetadata(field.getDisplayName(), value);
			}
		});
	}

	private void updateMetadata(final String field, final String value) {
		// if (db.queryForObject("select count(*) from " + METADATA_TABLE
		// + " where repo = ? and project = ? and field = ?",
		// Integer.class, gitRepo, project, field) > 0)
		// no real need to do an upsert here. all rows are generated by the
		// CloneTask autodetecting stuff.
		db.update("update " + METADATA_TABLE + " set value = ?"
				+ " where repo = ? and project = ? and field = ?", value,
				gitRepo, project, field);
		// else
		// db.update("insert into " + METADATA_TABLE
		// + "(repo, project, field, value) values(?, ?, ?, ?)",
		// gitRepo, project, field, value);
	}

	/**
	 * Populate metadata fields with values from autodetection. Only fields
	 * present in the set will be updated. This does not override the
	 * user-specified value set with {@link #setMetadata(MetadataField, String)}
	 * .
	 * 
	 * @param meta
	 *            {@link Map} of {@link MetadataField} to field value
	 */
	@Override
	public void setAutodetectedMetadata(final Map<MetadataField, String> meta) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				for (final Entry<MetadataField, String> e : meta.entrySet())
					updateAutodetectedMetadata(e.getKey().getDisplayName(),
							e.getValue());
			}
		});
	}

	/**
	 * Populate a single metadata fields with values from autodetection. This
	 * does not override the user-specified value set with
	 * {@link #setMetadata(MetadataField, String)}.
	 * 
	 * @param field
	 *            the {@link MetadataField} to change
	 * @param value
	 *            the field value, non-<code>null</code>
	 */
	public void setAutodetectedMetadata(final MetadataField field,
			final String value) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateAutodetectedMetadata(field.getDisplayName(), value);
			}
		});
	}

	private void updateAutodetectedMetadata(final String field,
			final String value) {
		final int entries = db.queryForObject("select count(*) from "
				+ METADATA_TABLE
				+ " where repo = ? and project = ? and field = ?",
				Integer.class, gitRepo, project, field);
		if (entries > 1)
			throw new IllegalStateException(entries + " entries for " + gitRepo
					+ ", " + project + ", " + field);

		if (entries == 0)
			db.update("insert into " + METADATA_TABLE
					+ "(repo, project, field, autodetected)"
					+ " values(?, ?, ?, ?)", gitRepo, project, field, value);
		else
			db.update("update " + METADATA_TABLE + " set autodetected = ?"
					+ " where repo = ? and project = ? and field = ?", value,
					gitRepo, project, field);
	}

	@Override
	public void setAutodetectedLicenses(final Map<Ref, LicenseFile> licenses) {
		// FIXME store to database instead!
		System.out.println(licenses);
	}

	/**
	 * Get the list of branches selected for archival / publication. More
	 * precisely, it provides a {@link RefAction} for each {@link Ref}, which
	 * can be a branch or a tag. Note that the {@link RefAction} includes the
	 * {@link Ref} as well; the Map is just for conveniently getting entries by
	 * Ref. The returned map is a snapshot; it doesn't reflect later changes
	 * made by {@link #setRefAction(Ref, PublicationMethod, String)}!
	 * 
	 * @return a {@link Map} of {@link Ref} to {@link RefAction}
	 */
	public Map<Ref, RefAction> getRefActions() {
		final Map<Ref, RefAction> actions = new HashMap<Ref, RefAction>();
		db.query("select ref, action, start from " + ACTION_TABLE
				+ " where repo = ? and project = ?", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final Ref ref = Ref.fromPath(rs.getString("ref"));
				final PublicationMethod publish = PublicationMethod.valueOf(rs
						.getString("action"));
				final String firstCommit = rs.getString("start");
				actions.put(ref, new RefAction(ref, publish, firstCommit));
			}
		}, gitRepo, project);
		return Collections.unmodifiableMap(actions);
	}

	/**
	 * Update the publication action for a single ref.
	 * 
	 * @param ref
	 *            the ref to update
	 * @param method
	 *            any valid {@link PublicationMethod}, or <code>null</code> to
	 *            delete the branch from the list (semantically: set it to
	 *            "don't publish")
	 * @param firstCommit
	 *            ID of first commit to archive. either the SHA-1 or
	 *            {@link RefAction#HEAD_COMMIT}
	 */
	public void setRefAction(final Ref ref, final PublicationMethod method,
			final String firstCommit) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateRefAction(ref.path,
						method != null ? method.name() : null, firstCommit);
			}
		});
	}

	private void updateRefAction(final String refPath, final String method,
			final String firstCommit) {
		db.update("delete from " + ACTION_TABLE
				+ " where repo = ? and project = ? and ref = ?", gitRepo,
				project, refPath);
		if (method == null)
			// setting method to null is the frontend's way of saying "branch
			// removed from list".
			// the only advantage of having to do upserts as delete / insert is
			// that this uncommon use case actually works out nicely...
			return;
		db.update("insert into " + ACTION_TABLE
				+ "(repo, project, ref, action, start) values(?, ?, ?, ?, ?)",
				gitRepo, project, refPath, method, firstCommit);
	}
}
