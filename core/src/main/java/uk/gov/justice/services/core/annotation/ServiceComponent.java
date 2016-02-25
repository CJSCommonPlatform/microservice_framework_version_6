package uk.gov.justice.services.core.annotation;


import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Identifies service components.
 * <p>
 * Usage: @ServiceComponent({@link Component#COMMAND_API})
 */

@Retention(RetentionPolicy.RUNTIME)
@ApplicationScoped
public @interface ServiceComponent {

    Component value();

}
