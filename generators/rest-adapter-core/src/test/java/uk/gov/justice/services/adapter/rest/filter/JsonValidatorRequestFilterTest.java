package uk.gov.justice.services.adapter.rest.filter;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class JsonValidatorRequestFilterTest {

    private static final MediaType TEST_JSON_MEDIA_TYPE = valueOf("application/vnd.test.action.name+json");

    @Mock
    private Logger logger;

    @Mock
    private HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @InjectMocks
    private JsonValidatorRequestFilter filterUnderTest;

    @Captor
    private ArgumentCaptor<InputStream> captor;


    @Test
    public void shouldAcceptValidJson() throws Exception {
        final String validJson = "{\"test_number\": 23, \"test_string\": \"twenty three\"}";

        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMediaType()).thenReturn(TEST_JSON_MEDIA_TYPE);
        when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(validJson.getBytes()));

        filterUnderTest.filter(context);

        verify(context).setEntityStream(captor.capture());

        final InputStream in = captor.getValue();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            assertThat(reader.readLine(), is(validJson));
        }
    }


    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidJson() throws Exception {
        final String invalidJson = "{\"test_number\": 23, \"test_string\": }";
        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        headers.add(USER_ID, randomUUID().toString());
        headers.add(ID, randomUUID().toString());

        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMediaType()).thenReturn(TEST_JSON_MEDIA_TYPE);
        when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(invalidJson.getBytes()));
        when(context.getHeaders()).thenReturn(headers);
        when(httpTraceLoggerHelper.toHttpHeaderTrace(any(MultivaluedMap.class))).thenReturn("{\"id\": \"4735e6a8-bbd5-4e38-a119-6a38e7de8a0b\"}");

        try {
            filterUnderTest.filter(context);
            fail("Expected BadRequestException to be thrown");
        }
        catch (final BadRequestException ex) {
            assertThat(ex.getMessage(), is("Invalid JSON provided to [{\"id\": \"4735e6a8-bbd5-4e38-a119-6a38e7de8a0b\"}] JSON: [{\"test_number\": 23, \"test_string\": }] "));
        }

        verify(context, never()).setEntityStream(any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidJsonHasTrailingComma() throws Exception {
        final String invalidJson = "{\"test_number\": 23, \"test_string\": \"twenty three\",}";

        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        headers.add(USER_ID, randomUUID().toString());
        headers.add(ID, randomUUID().toString());

        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMediaType()).thenReturn(TEST_JSON_MEDIA_TYPE);
        when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(invalidJson.getBytes()));
        when(context.getHeaders()).thenReturn(headers);
        when(httpTraceLoggerHelper.toHttpHeaderTrace(any(MultivaluedMap.class))).thenReturn("{\"id\": \"4735e6a8-bbd5-4e38-a119-6a38e7de8a0b\"}");

        try {
            filterUnderTest.filter(context);
            fail("Expected BadRequestException to be thrown");
        }
        catch (final BadRequestException ex) {
            assertThat(ex.getMessage(), is("Invalid JSON provided to [{\"id\": \"4735e6a8-bbd5-4e38-a119-6a38e7de8a0b\"}] JSON: [{\"test_number\": 23, \"test_string\": \"twenty three\",}] "));
        }

        verify(context, never()).setEntityStream(any());
    }


    @Test
    public void shouldAcceptEmptyPayload() throws Exception {
        final String payload = "";

        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMediaType()).thenReturn(TEST_JSON_MEDIA_TYPE);
        when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(payload.getBytes()));

        filterUnderTest.filter(context);

        verify(context).setEntityStream(captor.capture());

        final InputStream in = captor.getValue();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            assertThat(reader.readLine(), is(nullValue()));
        }
    }


    @Test
    public void shouldAcceptNoneJsonMediaType() throws Exception {
        final String payload = "NOT JSON DATA";

        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getMediaType()).thenReturn(MediaType.APPLICATION_XML_TYPE);
        when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(payload.getBytes()));

        filterUnderTest.filter(context);

        verify(context, never()).setEntityStream(any());
    }
}