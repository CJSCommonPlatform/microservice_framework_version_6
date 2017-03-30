package uk.gov.justice.services.adapter.rest.application;

import java.util.Set;

/**
 * Configuration interface providing common JAX-RS providers to be included in service level JAX-RS
 * application classes.
 */
public interface CommonProviders {

    Set<Class<?>> providers();
}