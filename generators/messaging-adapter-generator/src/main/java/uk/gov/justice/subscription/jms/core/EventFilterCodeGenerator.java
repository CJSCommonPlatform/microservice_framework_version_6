package uk.gov.justice.subscription.jms.core;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.gov.justice.subscription.jms.core.ClassNameFactory.EVENT_FILTER;

import uk.gov.justice.domain.subscriptiondescriptor.Event;
import uk.gov.justice.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.services.event.buffer.api.AbstractEventFilter;

import javax.enterprise.context.ApplicationScoped;

import com.squareup.javapoet.TypeSpec;

public class EventFilterCodeGenerator {

    TypeSpec generate(final Subscription subscription, final ClassNameFactory classNameFactory) {

        return classBuilder(classNameFactory.classNameFor(EVENT_FILTER))
                .addModifiers(PUBLIC)
                .superclass(AbstractEventFilter.class)
                .addAnnotation(ApplicationScoped.class)
                .addMethod(constructorBuilder()
                        .addModifiers(PUBLIC)
                        .addCode("super(\"$L\");", subscription.getEvents().stream().map(Event::getName)
                                .collect(joining("\",\""))).build())
                .build();
    }
}
