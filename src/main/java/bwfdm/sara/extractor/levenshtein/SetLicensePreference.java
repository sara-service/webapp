package bwfdm.sara.extractor.levenshtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import bwfdm.sara.db.ConfigDatabase;

public class SetLicensePreference {
	private final JdbcTemplate db;
	private final TransactionTemplate tx;

	public SetLicensePreference(final String driver, final String url,
			final String user, final String pass)
			throws ReflectiveOperationException {
		@SuppressWarnings("unchecked")
		final Class<Driver> drv = (Class<Driver>) Class.forName(driver);
		final SimpleDriverDataSource ds = new SimpleDriverDataSource(
				drv.newInstance(), url, user, pass);
		db = new JdbcTemplate(ds);
		tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
	}

	public void run() {
		tx.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(
					final TransactionStatus status) {
				try {
					execute();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private void execute() throws IOException {
		// move some preferred licenses to the top of the displayed list. reset
		// the rest of the preference order.
		final URL res = SetLicensePreference.class
				.getResource("preferred-licenses.txt");
		db.update("update " + ConfigDatabase.LICENSES_TABLE
				+ " set preference = null");
		int preference = 0;
		try (final BufferedReader data = new BufferedReader(
				new InputStreamReader(res.openStream()))) {
			while (true) {
				final String id = data.readLine();
				if (id == null)
					break;

				preference += 1000;
				db.update("update " + ConfigDatabase.LICENSES_TABLE
						+ " set preference = ? where id = ?", preference, id);
			}
		}
	}

	public static void main(final String... args)
			throws ReflectiveOperationException {
		if (args.length != 4) {
			System.err.println("usage: java "
					+ SetLicensePreference.class.getCanonicalName()
					+ " org.postgresql.Driver jdbc:postgresql://host/database"
					+ " username password");
			System.exit(1);
		}

		final String driver = args[0], url = args[1], user = args[2], pass = args[3];
		new SetLicensePreference(driver, url, user, pass).run();
	}
}
