package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import bwfdm.sara.Config;
import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.MetadataValue;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;

/**
 * JDBC-based implementation of {@link FrontendDatabase}. Should work with
 * PostgreSQL, MySQL (untested) and HSQL (embedded database used for
 * development) because it only uses trivial (CRUD, but without U) queries.
 */
public class FrontendDatabase {
	private static final String ACTION_TABLE = "frontend_actions";
	private static final String METADATA_TABLE = "frontend_metadata";

	private static final Map<MetadataField, MetadataValue> EMPTY_METADATA;
	static {
		// create an empty dummy entry for each metadata field. this ensures
		// that all fields exist, even when they're empty.
		final Map<MetadataField, MetadataValue> map = new EnumMap<>(
				MetadataField.class);
		for (final MetadataField f : MetadataField.values())
			map.put(f, new MetadataValue(null, true));
		EMPTY_METADATA = Collections.unmodifiableMap(map);
	}

	private final String gitRepo;
	private final String project;
	private final JdbcTemplate db;
	private final TransactionTemplate transaction;

	public FrontendDatabase(final String gitRepo, final Config config,
			final String project) {
		this.gitRepo = gitRepo;
		this.project = project;
		db = config.newJdbcTemplate();
		transaction = new TransactionTemplate(new DataSourceTransactionManager(
				db.getDataSource()));
	}

	/**
	 * Get metadata. All fields will always be present (JavaScript needs this);
	 * unset values map to an autodetected {@link MetadataValue} with a value of
	 * {@code null}. The returned map is a snapshot; it doesn't reflect later
	 * changes made by {@link #setMetadata(MetadataField, String, boolean)}!
	 * 
	 * @return all metadata fields in a {@link Map}
	 */
	public Map<MetadataField, MetadataValue> getMetadata() {
		final Map<MetadataField, MetadataValue> meta = new EnumMap<>(
				EMPTY_METADATA);
		db.query("select field, value, auto from " + METADATA_TABLE
				+ " where repo = ? and project = ?", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final MetadataField field = MetadataField.forDisplayName(rs
						.getString("field"));
				final MetadataValue value = new MetadataValue(rs
						.getString("value"), rs.getBoolean("auto"));
				meta.put(field, value);
			}
		}, gitRepo, project);
		return Collections.unmodifiableMap(meta);
	}

	/**
	 * Set a single metadata field.
	 * 
	 * @param field
	 *            the field to update, not {@code null}
	 * @param value
	 *            the new value, not {@code null} either
	 * @param auto
	 *            <code>true</code> if the value results from autodetection,
	 *            <code>false</code> if the user entered that value manually
	 */
	public void setMetadata(final MetadataField field, final String value,
			final boolean auto) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateMetadata(field.getDisplayName(), value, auto);
			}
		});
	}

	private void updateMetadata(final String name, final String value,
			final boolean auto) {
		db.update("delete from " + METADATA_TABLE
				+ " where repo = ? and project = ? and field = ?", gitRepo,
				project, name);
		db.update("insert into " + METADATA_TABLE
				+ "(repo, project, field, value, auto) values(?, ?, ?, ?, ?)",
				gitRepo, project, name, value, auto);
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
