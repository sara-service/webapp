package bwfdm.sara.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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
	private static final String ACTION_TABLE = "fe_temp_actions";
	private static final String METADATA_TABLE = "fe_temp_metadata";
	private static final String LICENSES_TABLE = "fe_temp_licenses";

	private final String gitRepo;
	private final String project;
	private final JacksonTemplate db;
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
		this.db = new JacksonTemplate(db);
		transaction = new TransactionTemplate(
				new DataSourceTransactionManager(db));
		// this doesn't really help with concurrent requests causing conflicts,
		// but it does lead to much less mysterious error messages ("could not
		// serialize" instead of a public key violation)
		transaction.setIsolationLevel(
				TransactionDefinition.ISOLATION_SERIALIZABLE);
	}

	/**
	 * Get metadata. The returned map is a snapshot; it doesn't reflect later
	 * changes made by {@link #setMetadata(MetadataField, String)}! Also, some
	 * fields will be missing if the user never entered a value for them.
	 *
	 * @return all metadata fields in a {@link Map}
	 */
	public Map<MetadataField, String> getMetadata() {
		return db.querySingleToMap(
				"select field, value from " + METADATA_TABLE
						+ " where repo = ? and project = ?",
				"field", MetadataField.class, "value", String.class, gitRepo,
				project);
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
		db.update(
				"delete from " + METADATA_TABLE
						+ " where repo = ? and project = ? and field = ?",
				gitRepo, project, field);
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
		return db.querySingleToMap(
				"select ref, license from " + LICENSES_TABLE
						+ " where repo = ? and project = ?",
				"ref", Ref.class, "license", String.class, gitRepo, project);
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
		db.update(
				"delete from " + LICENSES_TABLE
						+ " where repo = ? and project = ? and ref = ?",
				gitRepo, project, ref.path);
		if (value != null)
			db.update("insert into " + LICENSES_TABLE
					+ "(repo, project, ref, license) values(?, ?, ?, ?)",
					gitRepo, project, ref.path, value);
	}

	/**
	 * Get the list of branches selected for archival / publication. More
	 * precisely, it provides a list of {@link RefAction RefActions}. This holds
	 * both the {@link Ref} identifying the branch or tag, and the actual
	 * publication info. The returned list is a snapshot; it doesn't reflect
	 * changes made by {@link #setRefAction(Ref, PublicationMethod, String)}!
	 *
	 * @return a {@link List} of {@link RefAction}
	 */
	public List<RefAction> getRefActions() {
		return db.queryRowToList(
				"select ref, action, start from " + ACTION_TABLE
						+ " where repo = ? and project = ?",
				RefAction.class, gitRepo, project);
	}

	/**
	 * Get the list of branches selected for archival / publication. More
	 * precisely, it provides a list of {@link Ref Refs}, which can be a branch
	 * or a tag. The returned list is a snapshot; it doesn't reflect later
	 * changes made by {@link #setRefActions(Collection)}!
	 *
	 * @return a {@link List} of {@link Ref Refs}
	 */
	public List<Ref> getSelectedRefs() {
		return db.querySingleToList(
				"select ref from " + ACTION_TABLE
						+ " where repo = ? and project = ?",
				Ref.class, gitRepo, project);
	}

	/**
	 * Update the publication action for a project.
	 *
	 * @param actions
	 *            a list of {@link RefAction RefActions} listing actions for all
	 *            relevant refs. branches not in the list are marked as ignored.
	 */
	public void setRefActions(final Collection<RefAction> actions) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateRefActions(actions);
			}
		});
	}

	private void updateRefActions(final Collection<RefAction> actions) {
		db.update("delete from " + ACTION_TABLE
				+ " where repo = ? and project = ?", gitRepo, project);
		for (RefAction action : actions)
			db.update("insert into " + ACTION_TABLE
				+ "(repo, project, ref, action, start) values(?, ?, ?, ?, ?)",
					gitRepo, project, action.ref.path,
					action.publicationMethod.name(), action.firstCommit);
	}
}
