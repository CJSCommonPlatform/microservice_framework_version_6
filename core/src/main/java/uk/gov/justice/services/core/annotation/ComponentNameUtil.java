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
     * @param aClass The service component to be analysed
     * @return the component from the provided {@link ServiceComponent} or {@link Adapter}
     */
    public static String componentFrom(final Class<?> aClass) {
        if (aClass.isAnnotationPresent(ServiceComponent.class)) {
            return aClass.getAnnotation(ServiceComponent.class).value();
        } else if (aClass.isAnnotationPresent(Adapter.class)) {
            return aClass.getAnnotation(Adapter.class).value();
        } else if (aClass.isAnnotationPresent(FrameworkComponent.class)) {
            return aClass.getAnnotation(FrameworkComponent.class).value();
        } else if (aClass.isAnnotationPresent(CustomServiceComponent.class)) {
            return aClass.getAnnotation(CustomServiceComponent.class).value();
        } else if (aClass.isAnnotationPresent(CustomAdapter.class)) {
            return aClass.getAnnotation(CustomAdapter.class).value();
        } else if (aClass.isAnnotationPresent(DirectAdapter.class)) {
            return aClass.getAnnotation(DirectAdapter.class).value();
        } else {
            throw new IllegalStateException(format("No annotation found to define component for class %s", aClass));
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

    /**
     * Checks if the injection point class has a component annotation.
     *
     * @param injectionPoint the injection point to be analysed
     * @return true if the injection point has a component annotation
     */
    public static boolean hasComponentAnnotation(final InjectionPoint injectionPoint) {
        return hasComponentAnnotation(injectionPoint.getMember().getDeclaringClass());
    }

    private static Optional<String> fieldLevelComponent(final InjectionPoint injectionPoint) {
        if (injectionPoint.getMember() instanceof Field) {
            final Field field = (Field) injectionPoint.getMember();
            if (field.isAnnotationPresent(ServiceComponent.class)) {
                return Optional.of(field.getAnnotation(ServiceComponent.class).value());
            } else if (field.isAnnotationPresent(FrameworkComponent.class)) {
                return Optional.of(field.getAnnotation(FrameworkComponent.class).value());
            } else if (field.isAnnotationPresent(CustomServiceComponent.class)) {
                return Optional.of(field.getAnnotation(CustomServiceComponent.class).value());
            }
        }

        return empty();
    }

    private static boolean hasComponentAnnotation(final Class<?> aClass) {
        return aClass.isAnnotationPresent(ServiceComponent.class) ||
                aClass.isAnnotationPresent(Adapter.class) ||
                aClass.isAnnotationPresent(FrameworkComponent.class) ||
                aClass.isAnnotationPresent(CustomServiceComponent.class) ||
                aClass.isAnnotationPresent(CustomAdapter.class) ||
                aClass.isAnnotationPresent(DirectAdapter.class);
    }

}
