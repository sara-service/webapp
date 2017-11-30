package bwfdm.sara.extractor.levenshtein;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import bwfdm.sara.db.ConfigDatabase;

public class UpdateLicenses extends TransacionHelper {
	private static final String CHOOSEALICENSE_BASE = "https://choosealicense.com/licenses/";

	public UpdateLicenses(final String url, final String user, final String pass) {
		super(url, user, pass);
	}

	@Override
	public void run() throws IOException {
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

	public static void main(final String... args) {
		if (args.length != 3) {
			System.err
					.println("usage: java -cp … "
							+ UpdateLicenses.class.getCanonicalName()
							+ " jdbc:postgresql://host/database"
							+ " username password");
			System.exit(1);
		}
		new UpdateLicenses(args[0], args[1], args[2]).executeInTransaction();
	}
}
