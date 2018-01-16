package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jersey.repackaged.com.google.common.collect.Lists;

public class CollectionDAO extends DAOImpl {
	public final UUID id;
	public final String foreign_uuid;
	public final String display_name;
	public Boolean enabled;

	public static String TABLE = "Collection";
	public static List<String> FIELDS = Arrays.asList("id", "foreign_uuid", "display_name", "enabled");

	public CollectionDAO() {
		id = null;
		foreign_uuid = null;
		display_name = null;
		enabled = null;
	}

	@Override
	public List<String> getPrimaryKey() {
		return Lists.newArrayList("id", "display_name", "foreign_uuid");
	}
}
