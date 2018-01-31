package bwfdm.sara.publication.db;

import java.util.List;

import bwfdm.sara.publication.PublicationDatabase;

public class DAOImpl implements DAO {
	@Override
	public void dump() {
		System.out.println(this.getClass().getName());
		System.out.println("=========================");
		List<String> fields = PublicationDatabase.getDynamicFieldNames(this.getClass());
		fields.addAll(PublicationDatabase.getPrimaryKey(this.getClass()));
		for (String s : fields) {
			System.out.println(s + "==" + PublicationDatabase.getField(this, s));
		}

	}
}