package uk.gov.justice.services.adapters.test.utils.reflection;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    /**
     * @param clazz
     * @return - list of methods of the clazz
     */
    public static List<Method> methodsOf(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(m -> !m.getName().contains("jacoco") && !m.getName().contains("lambda"))
                .collect(toList());
    }

    /**
     * sets value of the field by reflaction
     *
     * @param object     - object to modify
     * @param fieldName  - name of the field belonging to the object
     * @param fieldValue - value of the field to be set
     * @throws IllegalAccessException
     */
    public static void setField(Object object, String fieldName, Object fieldValue)
            throws IllegalAccessException {
        Field field = fieldOf(object.getClass(), fieldName);
        field.setAccessible(true);
        field.set(object, fieldValue);
    }

    /**
     * Searches for a field in the given class by reflection
     *
     * @param clazz
     * @param fieldName
     * @return - field belonging to the given clazz with the given fieldName
     */
    public static Field fieldOf(Class<?> clazz, String fieldName) {
        Optional<Field> field = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getName().equals(fieldName))
                .findFirst();
        assertTrue(field.isPresent());
        return field.get();
    }

    /**
     * returns first method of the given class
     *
     * @param clazz
     * @return - first method of the given clazz
     */
    public static Method firstMethodOf(Class<?> clazz) {
        List<Method> methods = methodsOf(clazz);
        return methods.get(0);
    }

}
