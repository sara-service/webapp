package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

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
	public SortedSet<String> getPrimaryKey() {
		SortedSet<String> s = new TreeSet<String>();
		s.add("id");
		s.add("display_name");
		s.add("foreign_uuid");
		return s;
	}
}
