package uk.gov.justice.services.adapter.rest.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.adapter.rest.cors.CorsFeature;
import uk.gov.justice.services.adapter.rest.filter.LoggerRequestDataFilter;
import uk.gov.justice.services.adapter.rest.interceptor.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;
import uk.gov.justice.services.adapter.rest.mapper.ConflictedResourceExceptionMapper;
import uk.gov.justice.services.adapter.rest.mapper.ForbiddenRequestExceptionMapper;

import java.util.Set;

import org.junit.Test;

public class DefaultCommonProvidersTest {

    @Test
    public void shouldReturnAllCommonProviders() throws Exception {
        Set<Class<?>> providers = new DefaultCommonProviders().providers();
        assertThat(providers, containsInAnyOrder(
                BadRequestExceptionMapper.class,
                ConflictedResourceExceptionMapper.class,
                ForbiddenRequestExceptionMapper.class,
                JsonSchemaValidationInterceptor.class,
                LoggerRequestDataFilter.class,
                CorsFeature.class));
    }
}
