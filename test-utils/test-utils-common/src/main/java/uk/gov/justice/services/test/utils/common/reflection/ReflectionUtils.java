package uk.gov.justice.services.test.utils.common.reflection;

import java.lang.reflect.Field;

public class ReflectionUtils {

    /**
     * Sets variable by reflection down to 3 levels of abstracts classes.
     *
     * @param obj           - object to set the variable on
     * @param variableName  - name onf the variable to set
     * @param variableValue - value of the variable to be set
     */
    public static void setField(final Object obj, final String variableName, final Object variableValue) {
        Field declaredField = null;

        Class<?> clazz = obj.getClass();
        for (int i = 0; i <= 3; i++) {
            try {
                declaredField = clazz.getDeclaredField(variableName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }

        }

        declaredField.setAccessible(true);

        try {
            declaredField.set(obj, variableValue);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
