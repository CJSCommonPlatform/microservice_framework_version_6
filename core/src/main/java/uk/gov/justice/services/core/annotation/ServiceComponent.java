package uk.gov.justice.services.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;

/**
 * Identifies service components. <p> Usage: @ServiceComponent({@link Component#COMMAND_API})
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD})
@ApplicationScoped
public @interface ServiceComponent {

    String value();
}
