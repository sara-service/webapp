package bwfdm.sara.publication.db;

import java.lang.reflect.Field;
import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

public class DAOImpl implements DAO {
	
	@Override
	public void set(String fieldName, Object value) {
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, value);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object get(String fieldName) {
		//System.out.println(this.getClass().getName());
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			return field.get(this);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<String> getDynamicFieldNames() {
		try {
			List<String> fn = Lists.newArrayList();
			String fn_str;
			fn.clear();
			//Field[] fields = this.getClass().getDeclaredFields();
			Field[] fields = this.getClass().getFields();
			for (Field f : fields) {
				fn_str = f.getName();
				// treat all lower case members as DB fields
				if (fn_str.equals(fn_str.toLowerCase()))
					fn.add(fn_str);
			}
			return fn;
		}
		catch (SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<String> getPrimaryKey() {
		return Lists.newArrayList("uuid");
	}
	
	@Override
	public void dump() {
		System.out.println(this.getClass().getName());
		System.out.println("=========================");
		for (String s : getDynamicFieldNames()) {
			System.out.println( s + "==" + get(s));
		}
		
	}
}