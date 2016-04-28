package uk.gov.justice.services.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies handler methods for commands, queries and events. <p> Usage:
 * @Handles("context.command.do-something")
 */

@Retention(RUNTIME)
@Target(METHOD)
public @interface Handles {
    String value();

}
