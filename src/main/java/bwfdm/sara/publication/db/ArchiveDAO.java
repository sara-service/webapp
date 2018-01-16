package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jersey.repackaged.com.google.common.collect.Lists; // TODO get rid of it!!!!

public class ArchiveDAO extends DAOImpl {

	public final UUID uuid;
	public String display_name;
	public String contact_email;
	public String url;
	public String adapter;
	public String logo_base64;
	public Boolean enabled;

	public static String TABLE = "Archive";
	public static List<String> FIELDS = Arrays.asList("uuid", "display_name", "url", "contact_email", "adapter",
			"enabled");

	public ArchiveDAO() {
		uuid = null;
		display_name = null;
		url = null;
		contact_email = null;
		adapter = null;
		logo_base64 = null;
		enabled = null;
	}

	public List<String> getDynamicFieldNames() {
		List<String> fn = Lists.newArrayList();
		fn.clear();
		List<String> dyn_fn = super.getDynamicFieldNames();
		for (String s : FIELDS) {
			if (dyn_fn.contains(s)) {
				fn.add(s);
			} else {
				System.out.println("WARNING! " + s + " is used in FIELDS but not declared as member! Skipping...");
			}
		}
		return fn;
	}
}
