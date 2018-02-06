package bwfdm.sara.publication.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import bwfdm.sara.publication.MetadataValue;

class WhereClause<D extends DAO> {
	private static final Object[] NOTHING = {};

	private final List<String> fields;
	private final String where;

	public WhereClause(Class<? extends D> cls) {
		fields = new ArrayList<>();
		StringBuilder where = new StringBuilder();

		String verb = " where ";
		for (final Field f : cls.getFields())
			if (f.isAnnotationPresent(PrimaryKey.class)) {
				String name = f.getName();
				fields.add(name);
				where.append(verb).append(name).append("=?");
				verb = " and ";
			}
		this.where = where.toString();
	}

	public String getClause() {
		return where;
	}

	public Object[] getParams(final D obj) {
		return appendParams(NOTHING, obj);
	}

	public Object[] appendParams(Object[] existing, final D obj) {
		final Object[] params = new Object[existing.length + fields.size()];
		System.arraycopy(existing, 0, params, 0, existing.length);

		int i = existing.length;
		for (String f : fields)
			params[i++] = PublicationDatabase.getField(obj, f);
		return params;
	}

	public static void main(String[] args) {
		WhereClause<MetadataValue> where = new WhereClause<>(
				MetadataValue.class);
		System.out.println(where.getClause());
	}
}
