package uk.gov.justice.services.core.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies POJOs that are defining system events.
 * <p>
 * Usage: @Event("event-name")
 */

@Retention(RUNTIME)
@Target(TYPE)
public @interface Event {
    String value();
}
