package uk.gov.justice.services.core.annotation;

import javax.enterprise.inject.spi.InjectionPoint;

public enum ServiceComponentLocation {

    LOCAL,
    REMOTE;

    /**
     * Used by the AnnotationScanner to get the location for a given Class.  Checks for the
     * {@link Remote} annotation and returns REMOTE if present on the given Class.
     *
     * @param clazz the Class to check
     * @return the service component location
     */
    public static ServiceComponentLocation componentLocationFrom(final Class<?> clazz) {
        return (clazz.isAnnotationPresent(Remote.class) || clazz.isAnnotationPresent(Direct.class)) ? REMOTE : LOCAL;
    }

    /**
     * Used to get the location for a given InjectionPoint.  Checks
     * for {@link Adapter} or {@link CustomAdapter} annotations and returns LOCAL if present on the
     * Class containing the given InjectionPoint.
     *
     * @param injectionPoint the InjectionPoint to check
     * @return the service component location
     */
    public static ServiceComponentLocation componentLocationFrom(final InjectionPoint injectionPoint) {

        final Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return targetClass.isAnnotationPresent(Adapter.class)
                || targetClass.isAnnotationPresent(CustomAdapter.class)
                || targetClass.isAnnotationPresent(DirectAdapter.class) ? LOCAL : REMOTE;
    }
}
