package uk.gov.justice.services.core.annotation;


import static java.lang.String.format;
import static java.util.Optional.empty;

import java.lang.reflect.Field;
import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;

public final class ComponentNameUtil {

    private ComponentNameUtil() {
    }

    /**
     * Retrieves name of the component of the provided {@link ServiceComponent} or {@link Adapter}.
     *
     * @param clazz The service component to be analysed
     * @return the component from the provided {@link ServiceComponent} or {@link Adapter}
     */
    public static String componentFrom(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(ServiceComponent.class)) {
            return clazz.getAnnotation(ServiceComponent.class).value().name();
        } else if (clazz.isAnnotationPresent(Adapter.class)) {
            return clazz.getAnnotation(Adapter.class).value().name();
        } else if (clazz.isAnnotationPresent(FrameworkComponent.class)) {
            return clazz.getAnnotation(FrameworkComponent.class).value();
        } else if (clazz.isAnnotationPresent(CustomServiceComponent.class)) {
            return clazz.getAnnotation(CustomServiceComponent.class).value();
        } else if (clazz.isAnnotationPresent(CustomAdapter.class)) {
            return clazz.getAnnotation(CustomAdapter.class).value();
        } else {
            throw new IllegalStateException(format("No annotation found to define component for class %s", clazz));
        }
    }

    /**
     * Retrieves name of the the component of the provided injection point.
     *
     * @param injectionPoint the injection point to be analysed
     * @return the component from the provided injection point
     */
    public static String componentFrom(final InjectionPoint injectionPoint) {
        return fieldLevelComponent(injectionPoint)
                .orElseGet(() -> componentFrom(injectionPoint.getMember().getDeclaringClass()));
    }

    private static Optional<String> fieldLevelComponent(final InjectionPoint injectionPoint) {
        if (injectionPoint.getMember() instanceof Field) {
            final Field field = (Field) injectionPoint.getMember();
            if (field.isAnnotationPresent(ServiceComponent.class)) {
                return Optional.of(field.getAnnotation(ServiceComponent.class).value().name());
            } else if (field.isAnnotationPresent(FrameworkComponent.class)) {
                return Optional.of(field.getAnnotation(FrameworkComponent.class).value());
            } else if (field.isAnnotationPresent(CustomServiceComponent.class)) {
                return Optional.of(field.getAnnotation(CustomServiceComponent.class).value());
            }
        }

        return empty();
    }

}
