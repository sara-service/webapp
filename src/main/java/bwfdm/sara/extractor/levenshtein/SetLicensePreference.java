package bwfdm.sara.extractor.levenshtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import bwfdm.sara.db.ConfigDatabase;

public class SetLicensePreference extends TransacionHelper {
	public SetLicensePreference(final String url, final String user,
			final String pass) {
		super(url, user, pass);
	}

	@Override
	public void run() throws IOException {
		// move some preferred licenses to the top of the displayed list. reset
		// the rest of the preference order.
		final URL res = SetLicensePreference.class
				.getResource("preferred-licenses.txt");
		db.update("update " + ConfigDatabase.LICENSES_TABLE
				+ " set preference = default");
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

	public static void main(final String... args) {
		if (args.length != 3) {
			System.err
					.println("usage: java -cp … "
							+ SetLicensePreference.class.getCanonicalName()
							+ " jdbc:postgresql://host/database"
							+ " username password");
			System.exit(1);
		}
		new SetLicensePreference(args[0], args[1], args[2])
				.executeInTransaction();
	}
}
