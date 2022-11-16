package org.owizen.tntaggregation;

import java.lang.reflect.Field;

public interface IntrospectionHelper {

	default Object getFieldValue(Object subject, String fieldName) {
		try {
			Field field = getAccessibleField(subject.getClass(), fieldName);
			return field.get(subject);
		} catch (Exception e) {
			return null;
		}
	}

	default Field getAccessibleField(Class<?> cls, String fieldName) {
		do {
			try {
				Field field = cls.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				cls = cls.getSuperclass();
				continue;
			}
		} while (cls != null);

		return null;
	}

	default boolean setFieldValue(Object subject, String fieldName, Object value) {
		try {
			Field field = getAccessibleField(subject.getClass(), fieldName);
			field.set(subject, value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
