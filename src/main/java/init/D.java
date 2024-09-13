package init;

import init.paths.PATHS;
import init.settings.S;
import snake2d.LOG;
import snake2d.util.file.Json;

import java.lang.reflect.Field;

public final class D {
	
	private static Json currentJson;
	private static String currentClass;
	private static Json dd;
	
	private D() {

	}

	public static void init() {
		dd = new Json(PATHS.TEXT().getFolder("dictionary").get("Dic"));
	}


	
	public static void gInit(Class<?> clazz) {
		if (dd == null)
			return;
		if (!dd.has(clazz.getName())) {
			if (S.get().debug)
				LOG.err("No mapping for class: " + clazz.getName());
			currentJson = null;
			currentClass = null;
		}else {
			currentClass = clazz.getName();
			currentJson = dd.json(clazz.getName());
		}
	}
	
	public static void gInit(Object clazz) {
		gInit(clazz.getClass());
	}
	
	public static CharSequence g(String key, String def) {
		if (dd == null)
			return def;
		return g(key);
	}
	
	public static CharSequence g(String defKey) {
		if (dd == null)
			return defKey;
		if (currentJson == null || !currentJson.has(defKey)) {
			if (S.get().debug|| S.get().developer) {
				
				String ss = "";
				for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
					if (!e.getClassName().equals(D.class.getName()) && e.getClassName().indexOf(".Thread") < 0) {
						ss = "("+e.getClassName()+".java:" + e.getLineNumber() + ")";
						break;
					}
				}
				LOG.err("No mapping " + currentClass + " " + defKey + " " + ss);
			}
			return defKey;
		}
		return currentJson.text(defKey);
	}
	
	public static void t(Object clazz) {
		t(clazz.getClass(), clazz);
	}
	
	public static void t(Class<?> clazz) {
		t(clazz, null);
	}
	
	private static String old;
	
	public static String ts(Class<?> clazz) {
		String c = currentClass;
		t(clazz);
		if (c != null && dd != null) {
			currentClass = c;
			if (dd.has(c))
				currentJson = dd.json(c);
			else
				currentJson = null;
		}
		return "";
	}
	
	public static void spush(Class<?> clazz) {
		old = currentClass;
		t(clazz);
	}
	
	public static void spop() {
		if (old != null && dd != null) {
			currentClass = old;
			currentJson = dd.json(old);
		}
	}
	
	public static void t(Class<?> clazz, Object o) {
		
		gInit(clazz);
		if (currentJson == null)
			return;
		
		for (Field f : clazz.getDeclaredFields()) {
			
			if (CharSequence.class.isAssignableFrom(f.getType())) {
				String s = f.getName();
				if (s.length() > 1 && s.charAt(0) == '¤' && s.charAt(1) == '¤') {
					f.setAccessible(true);
					s = s.substring(2, s.length());
					CharSequence v = g(s);
					
					try {
						f.set(o, v);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
	}
	

}
