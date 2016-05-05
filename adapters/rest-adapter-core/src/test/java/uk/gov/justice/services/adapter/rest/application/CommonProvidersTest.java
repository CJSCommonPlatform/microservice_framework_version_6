package uk.gov.justice.services.adapter.rest.application;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.adapter.rest.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;

import java.util.Set;

import org.junit.Test;

public class CommonProvidersTest {

    @Test
    public void shouldReturnAllCommonProviders() throws Exception {
        Set<Class<?>> providers = new CommonProviders().providers();
        assertThat(providers, containsInAnyOrder(BadRequestExceptionMapper.class, JsonSchemaValidationInterceptor.class));
    }
}
