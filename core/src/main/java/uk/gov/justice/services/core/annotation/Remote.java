package uk.gov.justice.services.core.annotation;


import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks service components as being remote. <p> Usage: @Remote
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Remote {

}
