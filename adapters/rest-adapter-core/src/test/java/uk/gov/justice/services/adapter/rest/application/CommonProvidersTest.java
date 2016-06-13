package uk.gov.justice.services.adapter.rest.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.adapter.rest.interceptor.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.cors.CorsFeature;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;

import java.util.Set;

import org.junit.Test;

public class CommonProvidersTest {

    @Test
    public void shouldReturnAllCommonProviders() throws Exception {
        Set<Class<?>> providers = new CommonProviders().providers();
        assertThat(providers, containsInAnyOrder(BadRequestExceptionMapper.class, JsonSchemaValidationInterceptor.class, CorsFeature.class));
    }
}
