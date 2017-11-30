package bwfdm.sara.extractor.levenshtein;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import bwfdm.sara.db.ConfigDatabase;

public class UpdateLicenses {
	private static final Driver DRIVER = new Driver();
	private static final String CHOOSEALICENSE_BASE = "https://choosealicense.com/licenses/";

	private final JdbcTemplate db;
	private final TransactionTemplate tx;

	public UpdateLicenses(final String url, final String user, final String pass)
			throws ReflectiveOperationException {
		final SimpleDriverDataSource ds = new SimpleDriverDataSource(DRIVER,
				url, user, pass);
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
		// set foreign-key constraints to deferred. this way we can just upsert
		// using delete-and-insert, which is (still) less silly than using
		// merge to do an upsert.
		db.update("set constraints all deferred");

		// take all licenses on choosealicense.com and add to database. doesn't
		// remove licenses that are no longer present in the input!
		final ChoosealicenseParser parser = new ChoosealicenseParser();
		for (final File file : new File("choosealicense/_licenses").listFiles()) {
			try (final BufferedReader data = new BufferedReader(new FileReader(
					file))) {
				final LicenseMetadata meta = parser.parse(file);
				// TODO how reliable is the choosealicense.com site structure?
				final String url = CHOOSEALICENSE_BASE
						+ meta.getSpdxID().toLowerCase();

				db.update("delete from " + ConfigDatabase.LICENSES_TABLE
						+ " where id = ?", meta.getSpdxID());
				db.update("insert into " + ConfigDatabase.LICENSES_TABLE
						+ "(id, display_name, info_url, hidden, full_text)"
						+ " values(?, ?, ?, ?, ?)", meta.getSpdxID(),
						meta.getTitle(), url, meta.isHidden(),
						meta.getFullText());
			}
		}
	}

	public static void main(final String... args)
			throws ReflectiveOperationException {
		if (args.length != 3) {
			System.err
					.println("usage: java -cp … "
							+ UpdateLicenses.class.getCanonicalName()
							+ " jdbc:postgresql://host/database"
							+ " username password");
			System.exit(1);
		}

		final String url = args[0], user = args[1], pass = args[2];
		new UpdateLicenses(url, user, pass).run();
	}
}
