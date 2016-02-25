package uk.gov.justice.services.core.annotation;


import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Identifies adapters that receive messages from other service components.
 * <p>
 * Usage: @Adapter({@link Component#COMMAND_CONTROLLER})
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
@ApplicationScoped
public @interface Adapter {

    Component value();

}
