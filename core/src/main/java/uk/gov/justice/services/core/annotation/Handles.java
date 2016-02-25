package uk.gov.justice.services.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies handler methods for commands, queries and events.
 * <p>
 * Usage: @Handles("context.commands.do-something")
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Handles {
    String value();

}
