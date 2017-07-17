package bwfdm.sara.db;

import java.sql.ResultSet;
import java.sql.SQLException;
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
public class JDBCDatabase implements FrontendDatabase {
	private static final String ACTION_TABLE = "frontend_actions";
	private static final String METADATA_TABLE = "frontend_metadata";

	private final String gitRepo;
	private String project;
	private final JdbcTemplate db;
	private final TransactionTemplate transaction;

	public JDBCDatabase(final String gitRepo, final Config config) {
		this.gitRepo = gitRepo;
		db = config.newJdbcTemplate();
		transaction = new TransactionTemplate(new DataSourceTransactionManager(
				db.getDataSource()));
	}

	@Override
	public void setProjectPath(final String project) {
		this.project = project;
	}

	@Override
	public void loadMetadata(final Map<MetadataField, MetadataValue> meta) {
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
	}

	@Override
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

	@Override
	public void loadRefActions(final Map<Ref, RefAction> actions) {
		db.query("select ref, action, start from " + ACTION_TABLE
				+ " where repo = ? and project = ?", new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				final Ref ref = Ref.fromPath(rs.getString("ref"));
				final PublicationMethod publish = PublicationMethod.valueOf(rs
						.getString("action"));
				final String firstCommit = rs.getString("start");
				actions.put(ref, new RefAction(publish, firstCommit));
			}
		}, gitRepo, project);
	}

	@Override
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
