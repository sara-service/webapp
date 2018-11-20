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

import bwfdm.sara.project.ArchiveMetadata;
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
	private static final String ARCHIVE_TABLE = "fe_temp_archive";

	private final String gitRepo;
	private final String project;
	private final String user;
	private final JacksonTemplate db;
	private final TransactionTemplate transaction;

	/**
	 * Creates a DAO for reading / writing values for a particular project.
	 *
	 * @param db
	 *            the {@link DataSource} to use for all queries
	 * @param gitRepo
	 *            ID of the git repo, to qualify the project name
	 * @param project
	 *            the project name, used as database key together with gitRepo
	 * @param user
	 *            the user's unique user ID within the git repo
	 */
	public FrontendDatabase(final DataSource db, final String gitRepo,
			final String project, final String user) {
		this.gitRepo = gitRepo;
		this.project = project;
		this.user = user;
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
	 * Get metadata. The returned object is a snapshot; it doesn't reflect later
	 * changes made by {@link #setMetadata(MetadataField, String)}! Also, some
	 * fields will be <code>null</code> if the user never entered a value for
	 * them.
	 *
	 * @return all metadata fields as a {@link ArchiveMetadata} instance
	 */
	public ArchiveMetadata getMetadata() {
			return db.queryToObject("select title, description, version, master,"
					+ " submitter_surname, submitter_givenname from "
					+ METADATA_TABLE
					+ " where repo = ? and project = ? and uid = ?",
					ArchiveMetadata.class, gitRepo, project, user);
	}

	/**
	 * Set all metadata fields. <code>null</code> fields in the object result in
	 * SQL NULLs in the database.
	 *
	 * @param values
	 *            a {@link ArchiveMetadata} instance containing the new row
	 */
	public void setMetadata(final ArchiveMetadata values) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateMetadata(values);
			}
		});
	}

	private void updateMetadata(final ArchiveMetadata values) {
		db.update(
				"delete from " + METADATA_TABLE
						+ " where repo = ? and project = ? and uid = ?",
				gitRepo, project, user);
		db.update("insert into " + METADATA_TABLE
				+ "(repo, project, uid, title, description, version, master,"
				+ " submitter_surname, submitter_givenname)"
				+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?)", gitRepo, project,
				user, values.title, values.description, values.version,
				values.master, values.submitter.surname,
				values.submitter.givenname);
	}

	public void setArchiveAccess(final ArchiveAccess access) {
		transaction.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				updateArchiveAccess(access);
			}
		});
	}

	private void updateArchiveAccess(final ArchiveAccess access) {
		db.update(
				"delete from " + ARCHIVE_TABLE
						+ " where repo = ? and project = ? and uid = ?",
				gitRepo, project, user);
		db.update(
				"insert into " + ARCHIVE_TABLE
						+ "(repo, project, uid, access) values(?, ?, ?, ?)",
				gitRepo, project, user, access.getDisplayName());
	}

	public ArchiveAccess getArchiveAccess() {
		return db.queryToObject(
				"select access from " + ARCHIVE_TABLE
						+ " where repo = ? and project = ? and uid = ?",
				ArchiveAccess.class, gitRepo, project, user);
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
		return db.queryToMap(
				"select ref, license from " + LICENSES_TABLE
						+ " where repo = ? and project = ? and uid = ?",
				"ref", Ref.class, String.class, gitRepo, project, user);
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
				+ " where repo = ? and project = ? and uid = ? and ref = ?",
				gitRepo, project, user, ref.path);
		if (value != null)
			db.update("insert into " + LICENSES_TABLE
					+ "(repo, project, uid, ref, license) values(?, ?, ?, ?, ?)",
					gitRepo, project, user, ref.path, value);
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
		return db.queryToList(
				"select ref, action, start from " + ACTION_TABLE
						+ " where repo = ? and project = ? and uid = ?",
				RefAction.class, gitRepo, project, user);
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
		return db.queryToList(
				"select ref from " + ACTION_TABLE
						+ " where repo = ? and project = ? and uid = ?",
				Ref.class, gitRepo, project, user);
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
		db.update(
				"delete from " + ACTION_TABLE
						+ " where repo = ? and project = ? and uid = ?",
				gitRepo, project, user);
		for (RefAction action : actions)
			db.update("insert into " + ACTION_TABLE
					+ "(repo, project, uid, ref, action, start) values(?, ?, ?, ?, ?, ?)",
					gitRepo, project, user, action.ref.path,
					action.publicationMethod.name(), action.firstCommit);
	}
}
