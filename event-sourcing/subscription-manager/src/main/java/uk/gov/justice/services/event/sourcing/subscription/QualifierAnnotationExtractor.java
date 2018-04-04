package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class QualifierAnnotationExtractor {

    @SuppressWarnings("unchecked")
    public <T> T getFrom(final InjectionPoint injectionPoint, final Class<T> qualifierAnnotationClass) {

        final Set<Annotation> qualifiers = injectionPoint.getQualifiers();

        final Annotation qualifierAnnotation = qualifiers.stream()
                .filter(annotation -> annotation.annotationType().isAssignableFrom(qualifierAnnotationClass))
                .findFirst()
                .orElseThrow(() -> new SubscriptionManagerProducerException(format("Failed to find '%s' annotation on bean '%s'", qualifierAnnotationClass.getName(), injectionPoint.getBean().getName())));


        return (T) qualifierAnnotation;
    }
}
