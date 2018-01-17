package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jersey.repackaged.com.google.common.collect.Lists;

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
	public List<String> getPrimaryKey() {
		return Lists.newArrayList("metadata_mapping_uuid");
	}

}
