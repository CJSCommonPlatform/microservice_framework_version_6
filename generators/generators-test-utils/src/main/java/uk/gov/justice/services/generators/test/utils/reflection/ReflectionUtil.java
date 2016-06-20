package uk.gov.justice.services.generators.test.utils.reflection;


import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    /**
     * @return - list of methods of the clazz
     */
    public static List<Method> methodsOf(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(m -> !m.getName().contains("jacoco") && !m.getName().contains("lambda"))
                .collect(toList());
    }

    /**
     * sets value of the field by reflaction
     *
     * @param object     - object to modify
     * @param fieldName  - name of the field belonging to the object
     * @param fieldValue - value of the field to be set
     */
    public static void setField(final Object object, final String fieldName, final Object fieldValue)
            throws IllegalAccessException {
        final Field field = fieldOf(object.getClass(), fieldName);
        field.setAccessible(true);
        field.set(object, fieldValue);
    }

    /**
     * Searches for a field in the given class by reflection
     *
     * @return - field belonging to the given clazz with the given fieldName
     */
    public static Field fieldOf(final Class<?> clazz, final String fieldName) {
        final Optional<Field> field = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getName().equals(fieldName))
                .findFirst();
        assertTrue(field.isPresent());
        return field.get();
    }

    /**
     * returns first method of the given class
     *
     * @return - first method of the given clazz
     */
    public static Method firstMethodOf(final Class<?> clazz) {
        final List<Method> methods = methodsOf(clazz);
        return methods.get(0);
    }

    /**
     * returns method of the given class with the given name
     *
     * @return -  method of the given class with the given name
     */
    public static Method methodOf(final Class<?> clazz, final String methodName) {
        final List<Method> methods = methodsOf(clazz);
        final Optional<Method> method = methods.stream().filter(m -> m.getName().equals(methodName))
                .findFirst();
        assertTrue(method.isPresent());
        return method.get();
    }

}
