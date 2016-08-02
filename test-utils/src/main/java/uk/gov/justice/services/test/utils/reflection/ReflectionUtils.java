package uk.gov.justice.services.test.utils.reflection;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static void setField(Object obj1, String fieldName, Object fieldValue) {
        Field declaredField = null;
        try {
            try {
                declaredField = obj1.getClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                declaredField = obj1.getClass().getSuperclass().getDeclaredField(fieldName);
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }

        declaredField.setAccessible(true);

        try {
            declaredField.set(obj1, fieldValue);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
