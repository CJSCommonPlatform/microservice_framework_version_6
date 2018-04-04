package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;

import uk.gov.justice.services.core.cdi.SubscriptionName;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class SubscriptionNameAnnotationExtractor {

    public SubscriptionName getFrom(final InjectionPoint injectionPoint) {

        final Set<Annotation> qualifiers = injectionPoint.getQualifiers();

        final Annotation subscriptionNameAnnotation = qualifiers.stream()
                .filter(annotation -> annotation.annotationType() == SubscriptionName.class)
                .findFirst()
                .orElseThrow(() -> new SubscriptionManagerProducerException(format("Failed to find SubscriptionName annotation on EventListener '%s'", injectionPoint.getBean().getName())));


        return (SubscriptionName) subscriptionNameAnnotation;
    }
}
