package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class MetadataValueDAO extends DAOImpl {
	public UUID item_uuid;
	public UUID metadatamapping_uuid;
	public String map_from;
	public String data;

	public static String TABLE = "MetadataValue";
	public static List<String> FIELDS = Arrays.asList("item_uuid", "metadatamapping_uuid", "map_from", "data");

	public MetadataValueDAO() {
		item_uuid = null;
		metadatamapping_uuid = null;
		map_from = null;
		data = null;
	}

	@Override
	// FIXME this does NOT WORK!
	public SortedSet<String> getPrimaryKey() {
		SortedSet<String> s = new TreeSet<String>();
		s.add("metadata_mapping_uuid");
		s.add("map_from");
		s.add("item_uuid");
		return s;
	}

}
