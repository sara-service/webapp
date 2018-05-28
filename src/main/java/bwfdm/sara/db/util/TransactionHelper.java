package bwfdm.sara.db.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import bwfdm.sara.Config;

abstract class TransactionHelper {
	protected final JdbcTemplate db;
	protected final TransactionTemplate tx;

	protected TransactionHelper(final String... args) throws IOException {
		if (args.length != 1) {
			System.err.println("usage: " + getClass().getSimpleName()
					+ " database-config.properties");
			System.exit(1);
		}

		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(args[0])) {
			props.load(in);
		}
		final DataSource ds = Config.createDataSource(props);
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