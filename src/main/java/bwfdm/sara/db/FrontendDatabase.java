package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import bwfdm.sara.project.MetadataField;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;

/**
 * Database for the web frontend, storing the user's selections as they come in
 * over the REST API.
 * <p>
 * Implementation should work with PostgreSQL, MySQL (untested) and HSQL
 * (embedded database used for development) because it only uses trivial (CRUD,
 * but without U) queries.
 */
public class FrontendDatabase {
	private static final String ACTION_TABLE = "frontend_actions";
	private static final String METADATA_TABLE = "frontend_metadata";
	private static final String LICENSES_TABLE = "frontend_licenses";

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

	/**
	 * Get metadata. The returned map is a snapshot; it doesn't reflect later
	 * changes made by {@link #setMetadata(MetadataField, String)}! Also, some
	 * fields will be missing if the user never entered a value for them.
	 * 
	 * @return all metadata fields in a {@link Map}
	 */
	public Map<MetadataField, String> getMetadata() {
		final Map<MetadataField, String> meta = new EnumMap<>(
				MetadataField.class);
		db.query("select field, value from " + METADATA_TABLE
				+ " where repo = ? and project = ?", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final MetadataField field = MetadataField.forDisplayName(rs
						.getString("field"));
				meta.put(field, rs.getString("value"));
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
		db.update("delete from " + METADATA_TABLE
				+ " where repo = ? and project = ? and field = ?", gitRepo,
				project, field);
		if (value != null)
			db.update("insert into " + METADATA_TABLE
					+ "(repo, project, field, value) values(?, ?, ?, ?)",
					gitRepo, project, field, value);
	}

	/**
	 * Get per-branch license selection. The returned map is a snapshot; it
	 * doesn't reflect later changes made by {@link #setLicense(Ref, String)} or
	 * {@link #setLicenses(Map)}! Also, refs will not have a value unless the
	 * user chose a different license for that branch (or for all branches).
	 * 
	 * @return a {@link Map} giving the user-selected license for each branch /
	 *         tag
	 */
	public Map<Ref, String> getLicenses() {
		final Map<Ref, String> meta = new HashMap<>();
		db.query("select ref, license from " + LICENSES_TABLE
				+ " where repo = ? and project = ?", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final Ref ref = Ref.fromPath(rs.getString("ref"));
				meta.put(ref, rs.getString("license"));
			}
		}, gitRepo, project);
		return Collections.unmodifiableMap(meta);
	}

	/**
	 * Updates the license for a single ref.
	 * 
	 * @param ref
	 *            the ref whose license to set, never {@code null}
	 * @param value
	 *            the new license, or {@code null} to keep the existing license
	 */
	public void setLicense(final Ref ref, final String value) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateLicense(ref, value);
			}
		});
	}

	/**
	 * Updates the license for a set of refs. Only refs that have a
	 * corresponding key in the set will be modifed.
	 * 
	 * @param values
	 *            a {@link Map} providing a license for each ref. refs
	 *            explicitly mapped to {@code null} are set to keep the existing
	 *            license; refs that don't have a corresponding key in the set
	 *            are left unchanged.
	 */
	public void setLicenses(final Map<Ref, String> values) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				for (final Entry<Ref, String> e : values.entrySet())
					updateLicense(e.getKey(), e.getValue());
			}
		});
	}

	private void updateLicense(final Ref ref, final String value) {
		db.update("delete from " + LICENSES_TABLE
				+ " where repo = ? and project = ? and ref = ?", gitRepo,
				project, ref.path);
		if (value != null)
			db.update("insert into " + LICENSES_TABLE
					+ "(repo, project, ref, license) values(?, ?, ?, ?)",
					gitRepo, project, ref.path, value);
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
