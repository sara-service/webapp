package bwfdm.sara.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import bwfdm.sara.db.ConfigDatabase;

public class UpdateLicenses extends TransacionHelper {
	private static final String CHOOSEALICENSE_BASE = "https://choosealicense.com/licenses/";

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
		new UpdateLicenses().executeInTransaction();
	}
}
