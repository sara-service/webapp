package bwfdm.sara.db.util;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import bwfdm.sara.Config;

abstract class TransacionHelper {
	protected final JdbcTemplate db;
	protected final TransactionTemplate tx;

	protected TransacionHelper() {
		final DataSource ds;
		try {
			ds = new Config(Config.SPRING_APPLICATION_CONFIG_FILE)
					.getDatabase();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
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