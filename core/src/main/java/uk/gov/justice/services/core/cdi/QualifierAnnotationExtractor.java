package uk.gov.justice.services.core.cdi;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Extracts a qualifier from an {@link InjectionPoint}
 */
@ApplicationScoped
public class QualifierAnnotationExtractor {

    /**
     * Extract a qualifier from an {@link InjectionPoint}
     *
     * @param injectionPoint           the {@link InjectionPoint} to extract from
     * @param qualifierAnnotationClass the qualifier class to extract
     * @param <T>                      the qualifier type
     * @return the qualifier of type T
     */
    @SuppressWarnings("unchecked")
    public <T> T getFrom(final InjectionPoint injectionPoint, final Class<T> qualifierAnnotationClass) {

        final Set<Annotation> qualifiers = injectionPoint.getQualifiers();

        final Annotation qualifierAnnotation = qualifiers.stream()
                .filter(annotation -> annotation.annotationType().isAssignableFrom(qualifierAnnotationClass))
                .findFirst()
                .orElseThrow(() -> new InjectionException(format("Failed to find '%s' annotation on bean '%s'", qualifierAnnotationClass.getName(), injectionPoint.getBean().getName())));


        return (T) qualifierAnnotation;
    }
}
