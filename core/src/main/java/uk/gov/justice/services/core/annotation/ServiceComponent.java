package uk.gov.justice.services.core.annotation;


import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies service components.
 * <p>
 * Usage: @ServiceComponent({@link Component#COMMAND_API})
 */

@Retention(RUNTIME)
@Target(TYPE)
@ApplicationScoped
public @interface ServiceComponent {

    Component value();

}
