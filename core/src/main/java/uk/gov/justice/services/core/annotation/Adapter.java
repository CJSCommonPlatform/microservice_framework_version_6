package uk.gov.justice.services.core.annotation;


import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;

/**
 * Identifies adapters that receive messages from other service components. <p> Usage:
 * @Adapter({@link Component#COMMAND_CONTROLLER})
 */

@Retention(RUNTIME)
@Target(TYPE)
@ApplicationScoped
public @interface Adapter {

    Component value();

}
