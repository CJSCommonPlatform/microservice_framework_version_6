package uk.gov.justice.services.core.cdi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

// @TODO: move this to framework-api
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface EventSourceName {

    String DEFAULT_EVENT_SOURCE_NAME = "defaultEventSource";

    String value() default DEFAULT_EVENT_SOURCE_NAME;
}
