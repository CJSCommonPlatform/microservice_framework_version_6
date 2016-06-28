package uk.gov.justice.services.core.annotation;


import javax.enterprise.context.ApplicationScoped;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies provider classes, which are used by the access-control module to inject information
 * into the rules engine.
 */

@Retention(RUNTIME)
@Target(TYPE)
@ApplicationScoped
public @interface Provider {

}
