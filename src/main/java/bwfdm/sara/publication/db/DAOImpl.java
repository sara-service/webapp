package bwfdm.sara.publication.db;

/**
 * @author sk
 */

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class DAOImpl implements DAO {
	// FIXME all of these methods could go into PublicationDatabase instead

	@Override
	public void set(String fieldName, Object value) {
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, value);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object get(String fieldName) {
		// System.out.println(this.getClass().getName());
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			return field.get(this);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public final List<String> getDynamicFieldNames() {
		return getDynamicFieldNames(this.getClass());
	}

	public static List<String> getDynamicFieldNames(Class<? extends DAO> cls) {
		final List<String> fields = new ArrayList<>();
		for (final Field f : cls.getFields())
			if (f.isAnnotationPresent(DatabaseField.class))
				fields.add(f.getName());
		return fields;
	}

	@Override
	public final SortedSet<String> getPrimaryKey() {
		return getPrimaryKey(this.getClass());
	}

	public static SortedSet<String> getPrimaryKey(Class<? extends DAO> cls) {
		final SortedSet<String> fields = new TreeSet<>();
		for (final Field f : cls.getFields())
			if (f.isAnnotationPresent(PrimaryKey.class))
				fields.add(f.getName());
		return fields;
	}

	@Override
	public void dump() {
		System.out.println(this.getClass().getName());
		System.out.println("=========================");
		List<String> fields = getDynamicFieldNames();
		fields.addAll(getPrimaryKey());
		for (String s : fields) {
			System.out.println(s + "==" + get(s));
		}

	}
}