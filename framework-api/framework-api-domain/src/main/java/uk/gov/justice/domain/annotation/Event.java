package uk.gov.justice.domain.annotation;


import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies POJOs that are defining system events. <p> Usage: @Event("event-name")
 */

@Retention(RUNTIME)
@Target(TYPE)
public @interface Event {
    String value();
}
