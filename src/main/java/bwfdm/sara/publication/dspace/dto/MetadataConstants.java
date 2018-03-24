package bwfdm.sara.publication.dspace.dto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @author vk
 */
public class MetadataConstants {

	// DataCite minimum fields:
	//
	// - Identifier
	// - Creator
	// - Title
	// - Publisher
	// - Publication Year
	// - Resource Type

	public static String ITEM_METADATA_EXAMPLE = "[" + "{" + "\"key\"" + ":"
			+ "\"dc.identifier.doi\"" + ","//
			+ "\"value\"" + ":" + "\"test-doi\"" + ","//
			+ "\"language\"" + ":" + "\"null\""//
			+ "}" + ","//
			+ "{" + "\"key\"" + ":" + "\"dc.contributor.author\"" + ","//
			+ "\"value\"" + ":" + "\"test-contributor\"" + ","//
			+ "\"language\"" + ":" + "\"null\""//
			+ "}" + ","//
			+ "{" + "\"key\"" + ":" + "\"dc.title\"" + ","//
			+ "\"value\"" + ":" + "\"test-title\"" + ","//
			+ "\"language\"" + ":" + "\"null\""//
			+ "}" + ","//
			+ "{" + "\"key\"" + ":" + "\"dc.publisher\"" + ","//
			+ "\"value\"" + ":" + "\"test-publisher\"" + ","//
			+ "\"language\"" + ":" + "\"null\""//
			+ "}" + ","//
			+ "{" + "\"key\"" + ":" + "\"dc.date.issued\"" + ","//
			+ "\"value\"" + ":" + "\"test-title\"" + ","//
			+ "\"language\"" + ":" + "\"null\""//
			+ "}" + ","//
			+ "{" + "\"key\"" + ":" + "\"dc.type\"" + ","//
			+ "\"value\"" + ":" + "\"test-type\"" + ","//
			+ "\"language\"" + ":" + "\"null\""//
			+ "}" + ","//
			+ "{" + "\"key\"" + ":" + "\"uulm.typeDCMI\"" + ","//
			+ "\"value\"" + ":" + "\"test-type-UULM\"" + ","//
			+ "\"language\"" + ":" + "\"null\""//
			+ "}"//
			+ "]";

	public static String ITEM_BITSTREAM_DESCRIPTION_EXAMPLE = "{" + "\"name\":"
			+ "\"" + "test-bitstream" + "\"" + "}";

	static {
		ITEM_METADATA_EXAMPLE = read("item_metadata_example.json");
		ITEM_BITSTREAM_DESCRIPTION_EXAMPLE = read("item_bitstream_description_example.json");
	}

	private static String read(final String name) {
		final BufferedReader br = new BufferedReader(new InputStreamReader(
				MetadataConstants.class.getResourceAsStream(name)));
		final StringBuilder text = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null)
				text.append(line);
			br.close();
		} catch (final IOException e) {
			// IO error to a resource really shouldn't happen...
			throw new RuntimeException(e);
		}
		return text.toString();
	}
}
