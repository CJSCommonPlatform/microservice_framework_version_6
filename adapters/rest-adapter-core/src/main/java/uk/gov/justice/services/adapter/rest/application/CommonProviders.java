package uk.gov.justice.services.adapter.rest.application;


import uk.gov.justice.services.adapter.rest.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class containing common JAX-RS providers to be included in service level JAX-RS
 * application classes.
 */
public class CommonProviders {

    public Set<Class<?>> providers() {
        final HashSet<Class<?>> classes = new HashSet<>();
        classes.add(BadRequestExceptionMapper.class);
        classes.add(JsonSchemaValidationInterceptor.class);
        return classes;
    }

}
