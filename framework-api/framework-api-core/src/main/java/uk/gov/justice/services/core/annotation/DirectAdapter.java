package uk.gov.justice.services.core.annotation;


import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;

/**
 * Identifies direct adapters that receive messages directly from other service components. <p> Usage:
 * "@DirectAdapter(value={@link Component#COMMAND_HANDLER}, actions={"action1", "action2"})"
 */
@Retention(RUNTIME)
@Target(TYPE)
@ApplicationScoped
public @interface DirectAdapter {

    String component();
    String[] actions();

}
