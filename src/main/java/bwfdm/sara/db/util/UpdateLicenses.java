package bwfdm.sara.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import bwfdm.sara.db.ConfigDatabase;

public class UpdateLicenses extends TransactionHelper {
	private static final String CHOOSEALICENSE_BASE = "https://choosealicense.com/licenses/";

	public UpdateLicenses(String... args) throws IOException {
		super(args);
	}

	@Override
	public void run() throws IOException {
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

				db.update("insert into " + ConfigDatabase.LICENSES_TABLE
						+ "(id, display_name, info_url, hidden, full_text)"
						+ " values(?, ?, ?, ?, ?) "
						+ "on conflict (id) do update set"
						+ " display_name = excluded.display_name,"
						+ " info_url = excluded.info_url,"
						+ " hidden = excluded.hidden,"
						+ " full_text = excluded.full_text", meta.getSpdxID(),
						meta.getTitle(), url, meta.isHidden(),
						meta.getFullText());
			}
		}
	}

	public static void main(final String... args) throws IOException {
		new UpdateLicenses(args).executeInTransaction();
	}
}
