package bwfdm.sara.extractor.levenshtein;

import java.io.IOException;

import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

abstract class TransacionHelper {
	private static final Driver DRIVER = new Driver();
	protected final JdbcTemplate db;
	protected final TransactionTemplate tx;

	protected TransacionHelper(final String url, final String user,
			final String pass) {
		final SimpleDriverDataSource ds = new SimpleDriverDataSource(DRIVER,
				url, user, pass);
		db = new JdbcTemplate(ds);
		tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
	}

	public void executeInTransaction() {
		tx.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				try {
					run();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	protected abstract void run() throws IOException;
}