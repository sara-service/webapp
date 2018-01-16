package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jersey.repackaged.com.google.common.collect.Lists;

public class MetadataDAO extends DAOImpl {
	public final UUID id;
	public String display_name;
	public final String map_from;
	public final String map_to;
	public Boolean enabled;

	public static String TABLE = "Metadata";
	public static List<String> FIELDS = Arrays.asList("id", "display_name", "map_from", "map_to", "enabled");

	public MetadataDAO() {
		id = null;
		display_name = null;
		enabled = null;
		map_from = null;
		map_to = null;
	}

	@Override
	public List<String> getPrimaryKey() {
		return Lists.newArrayList("id", "map_from", "map_to");
	}
}
