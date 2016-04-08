package uk.gov.justice.services.core.annotation;

import javax.enterprise.inject.spi.InjectionPoint;

public enum ServiceComponentLocation {

    LOCAL,
    REMOTE;

    public static ServiceComponentLocation componentLocationFrom(final Class<?> clazz) {
        return clazz.isAnnotationPresent(Remote.class) ? REMOTE : LOCAL;
    }

    public static ServiceComponentLocation componentLocationFrom(final InjectionPoint injectionPoint) {
        final Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return targetClass.isAnnotationPresent(Adapter.class) ? LOCAL : REMOTE;
    }
}
