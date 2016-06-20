package uk.gov.justice.services.adapter.rest.cors;

import static java.lang.String.valueOf;
import static javax.ws.rs.HttpMethod.OPTIONS;
import static javax.ws.rs.HttpMethod.POST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_MAX_AGE;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static uk.gov.justice.services.adapter.rest.cors.CorsHeaders.ORIGIN;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link CorsFilter} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CorsFilterTest {

    private static final String CORS_FAILURE_PROPERTY_NAME = "cors.failure";

    private static final String TEST_ORIGIN = "test";

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    private MultivaluedMap<String, Object> headers;

    private CorsFilter corsFilter;

    @Before
    public void setup() {
        when(requestContext.getHeaderString(ORIGIN)).thenReturn(TEST_ORIGIN);

        headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);

        corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add(TEST_ORIGIN);
    }

    @Test(expected = ForbiddenException.class)
    public void shouldThrowExceptionIfOriginCheckForPostFailed() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        when(requestContext.getHeaderString(ORIGIN)).thenReturn("some-other-origin");
        corsFilter.filter(requestContext);
    }

    @Test
    public void shouldSetCorsFailurePropertyIfOriginCheckForPostFailed() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        when(requestContext.getHeaderString(ORIGIN)).thenReturn("some-other-origin");

        try {
            corsFilter.filter(requestContext);
        } catch(ForbiddenException ex) {
            // do nothing because we want to check the requestContext
        }

        verify(requestContext).setProperty(CORS_FAILURE_PROPERTY_NAME, true);
    }

    @Test
    public void shouldAllowPostIfOriginCheckSucceeds() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        corsFilter.filter(requestContext);
    }

    @Test
    public void shouldAllowPostIfOriginIsNull() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        when(requestContext.getHeaderString(ORIGIN)).thenReturn(null);
        corsFilter.filter(requestContext);
    }

    @Test
    public void shouldSetAllowedOriginsHeader() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);

        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_ALLOW_ORIGIN), equalTo(TEST_ORIGIN));
    }

    @Test
    public void shouldSetAllowCredentialsIfEnabled() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);

        corsFilter.setAllowCredentials(true);
        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_ALLOW_CREDENTIALS), equalTo("true"));
    }

    @Test
    public void shouldNotSetAllowCredentialsIfDisabled() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);

        corsFilter.setAllowCredentials(false);
        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_ALLOW_CREDENTIALS), nullValue());
    }

    @Test
    public void shouldSetAllowMethodsHeaderFromRequest() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);
        when(requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(POST);

        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_ALLOW_METHODS), equalTo(POST));
    }

    @Test
    public void shouldSetAllowMethodsHeaderFromConfiguration() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);
        when(requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(POST);

        final String configuredAllowedMethods = "some-other-method";
        corsFilter.setAllowedMethods(configuredAllowedMethods);
        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_ALLOW_METHODS), equalTo(configuredAllowedMethods));
    }

    @Test
    public void shouldSetAllowHeadersHeaderFromRequest() throws Exception {
        final String allowHeader = "some-header-name";
        when(requestContext.getMethod()).thenReturn(OPTIONS);
        when(requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn(allowHeader);

        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_ALLOW_HEADERS), equalTo(allowHeader));
    }

    @Test
    public void shouldSetAllowHeadersHeaderFromConfiguration() throws Exception {
        final String allowHeader = "some-header-name";
        final String configuredAllowedHeader = "some-other-header-name";
        when(requestContext.getMethod()).thenReturn(OPTIONS);
        when(requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn(allowHeader);

        corsFilter.setAllowedHeaders(configuredAllowedHeader);
        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_ALLOW_HEADERS), equalTo(configuredAllowedHeader));
    }

    @Test
    public void shouldSetMaxAgeIfEnabled() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);

        final int corsMaxAge = 9999;
        corsFilter.setCorsMaxAge(corsMaxAge);
        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_MAX_AGE), equalTo(valueOf(corsMaxAge)));
    }

    @Test
    public void shouldNotSetMaxAgeIfDisabled() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);

        corsFilter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertThat(captor.getValue().getHeaderString(ACCESS_CONTROL_MAX_AGE), nullValue());
    }

    @Test
    public void shouldDoNothingIfOriginIsNotSet() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        when(requestContext.getHeaderString(ORIGIN)).thenReturn(null);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_ORIGIN), nullValue());
        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS), nullValue());
        assertThat(headers.getFirst(ACCESS_CONTROL_EXPOSE_HEADERS), nullValue());
    }

    @Test
    public void shouldDoNothingForOptionsRequest() throws Exception {
        when(requestContext.getMethod()).thenReturn(OPTIONS);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_ORIGIN), nullValue());
        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS), nullValue());
        assertThat(headers.getFirst(ACCESS_CONTROL_EXPOSE_HEADERS), nullValue());
    }

    @Test
    public void shouldDoNothingIfCorsFailureAlreadyOccurred() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        when(requestContext.getProperty(CORS_FAILURE_PROPERTY_NAME)).thenReturn(true);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_ORIGIN), nullValue());
        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS), nullValue());
        assertThat(headers.getFirst(ACCESS_CONTROL_EXPOSE_HEADERS), nullValue());
    }

    @Test
    public void shouldSetAllowedOriginHeaderIfOriginSet() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_ORIGIN), equalTo(TEST_ORIGIN));
    }

    @Test
    public void shouldSetAllowCredentialsHeaderIfEnabled() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        corsFilter.setAllowCredentials(true);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS), equalTo("true"));
    }

    @Test
    public void shouldNotSetAllowCredentialsHeaderIfDisabled() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);
        corsFilter.setAllowCredentials(false);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS), nullValue());
    }

    @Test
    public void shouldSetExposeHeadersIfPresent() throws Exception {
        final String exposedHeaders = "some-header";
        when(requestContext.getMethod()).thenReturn(POST);
        corsFilter.setExposedHeaders(exposedHeaders);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_EXPOSE_HEADERS), equalTo(exposedHeaders));
    }

    @Test
    public void shouldNotSetExposeHeadersIfAbsent() throws Exception {
        when(requestContext.getMethod()).thenReturn(POST);

        corsFilter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(ACCESS_CONTROL_EXPOSE_HEADERS), nullValue());
    }

    @Test
    public void shouldReturnAllowCredentials() {
        assertThat(corsFilter.isAllowCredentials(), equalTo(true));
        corsFilter.setAllowCredentials(false);
        assertThat(corsFilter.isAllowCredentials(), equalTo(false));
    }

    @Test
    public void shouldReturnAllowedMethods() {
        final String allowedMethods = "test-methods";
        corsFilter.setAllowedMethods(allowedMethods);
        assertThat(corsFilter.getAllowedMethods(), equalTo(allowedMethods));
    }

    @Test
    public void shouldReturnAllowedHeaders() {
        final String allowedHeaders = "test-headers";
        corsFilter.setAllowedHeaders(allowedHeaders);
        assertThat(corsFilter.getAllowedHeaders(), equalTo(allowedHeaders));
    }

    @Test
    public void shouldReturnExposedHeaders() {
        final String exposedHeaders = "test-headers";
        corsFilter.setExposedHeaders(exposedHeaders);
        assertThat(corsFilter.getExposedHeaders(), equalTo(exposedHeaders));
    }

    @Test
    public void shouldReturnCorsMaxAge() {
        final int corsMaxAge = 9999;
        assertThat(corsFilter.getCorsMaxAge(), equalTo(-1));
        corsFilter.setCorsMaxAge(corsMaxAge);
        assertThat(corsFilter.getCorsMaxAge(), equalTo(corsMaxAge));
    }
}
