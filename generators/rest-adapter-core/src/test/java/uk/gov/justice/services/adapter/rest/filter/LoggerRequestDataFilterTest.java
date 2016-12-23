package uk.gov.justice.services.adapter.rest.filter;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.NAME;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.ByteArrayInputStream;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class LoggerRequestDataFilterTest {

    private static final String MESSAGE_ID_VALUE = randomUUID().toString();
    private static final String CONTENT_TYPE_VALUE = "application/content";
    private static final String ACCEPT_VALUE = "application/response";
    private static final String CLIENT_CORRELATION_ID_VALUE = randomUUID().toString();
    private static final String SESSION_ID_VALUE = randomUUID().toString();
    private static final String NAME_VALUE = "context.name";
    private static final String USER_ID_VALUE = randomUUID().toString();
    private static final String SERVICE_CONTEXT_NAME_VALUE = "service.name";

    @Mock
    private Logger logger;

    @Mock
    private ContainerRequestContext context;

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

    @InjectMocks
    private LoggerRequestDataFilter loggerRequestDataFilter;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddMetadataFromHeadersToMappedDiagnosticContextOnRequest() throws Exception {
        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        headers.putSingle(ID, MESSAGE_ID_VALUE);
        headers.putSingle(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        headers.putSingle(CLIENT_CORRELATION_ID, CLIENT_CORRELATION_ID_VALUE);
        headers.putSingle(SESSION_ID, SESSION_ID_VALUE);
        headers.putSingle(NAME, NAME_VALUE);
        headers.putSingle(USER_ID, USER_ID_VALUE);
        headers.putSingle(ACCEPT, ACCEPT_VALUE);

        when(context.getMediaType()).thenReturn(new MediaType("", ""));
        when(context.getHeaders()).thenReturn(headers);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(SERVICE_CONTEXT_NAME_VALUE);

        loggerRequestDataFilter.filter(context);

        assertThat(MDC.get(REQUEST_DATA), isJson(
                allOf(
                        withJsonPath("$." + METADATA + ".id", equalTo(MESSAGE_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".correlation.client", equalTo(CLIENT_CORRELATION_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".context.session", equalTo(SESSION_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".name", equalTo(NAME_VALUE)),
                        withJsonPath("$." + METADATA + ".context.user", equalTo(USER_ID_VALUE)),
                        withJsonPath("$." + CONTENT_TYPE, equalTo(CONTENT_TYPE_VALUE)),
                        withJsonPath("$." + ACCEPT, equalTo(ACCEPT_VALUE)),
                        withJsonPath("$." + SERVICE_CONTEXT, equalTo(SERVICE_CONTEXT_NAME_VALUE))))
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldClearMappedDiagnosticContextOnResponse() throws Exception {
        final String messageId = randomUUID().toString();
        final String name = "context.name";

        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        headers.putSingle(ID, messageId);
        headers.putSingle(NAME, name);

        when(context.getMediaType()).thenReturn(new MediaType("", ""));
        when(context.getHeaders()).thenReturn(headers);

        loggerRequestDataFilter.filter(context);
        assertThat(MDC.get(REQUEST_DATA), isJson(
                allOf(
                        withJsonPath("$." + METADATA + ".id", equalTo(messageId)),
                        withJsonPath("$." + METADATA + ".name", equalTo(name))
                )));

        loggerRequestDataFilter.filter(context, mock(ContainerResponseContext.class));
        assertThat(MDC.get(REQUEST_DATA), nullValue());
    }

    @Test
    @SuppressWarnings({"unchecked", "deprecation"})
    public void shouldGetMetadataFromPayload() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(MESSAGE_ID_VALUE, NAME_VALUE)
                        .withClientCorrelationId(CLIENT_CORRELATION_ID_VALUE)
                        .withSessionId(SESSION_ID_VALUE)
                        .withUserId(USER_ID_VALUE))
                .withPayloadOf("data", "someData")
                .build();

        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();

        when(context.getHeaders()).thenReturn(headers);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(SERVICE_CONTEXT_NAME_VALUE);
        when(context.getMediaType()).thenReturn(new MediaType("application/test", "+json", "UTF-8"));
        when(context.getEntityStream()).thenReturn(toInputStream(jsonEnvelope.toDebugStringPrettyPrint(), "UTF-8"));

        loggerRequestDataFilter.filter(context);

        assertThat(MDC.get(REQUEST_DATA), isJson(
                allOf(
                        withJsonPath("$." + METADATA + ".id", equalTo(MESSAGE_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".correlation.client", equalTo(CLIENT_CORRELATION_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".context.session", equalTo(SESSION_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".name", equalTo(NAME_VALUE)),
                        withJsonPath("$." + METADATA + ".context.user", equalTo(USER_ID_VALUE)),
                        withJsonPath("$." + SERVICE_CONTEXT, equalTo(SERVICE_CONTEXT_NAME_VALUE))))
        );

        verify(context).setEntityStream(Mockito.any(ByteArrayInputStream.class));
    }

    @Test
    @SuppressWarnings({"unchecked", "deprecation"})
    public void shouldMergeDataInHeadersWithPayloadMetadata() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(MESSAGE_ID_VALUE, NAME_VALUE)
                        .withClientCorrelationId(CLIENT_CORRELATION_ID_VALUE))
                .withPayloadOf("data", "someData")
                .build();

        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        headers.putSingle(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        headers.putSingle(ACCEPT, ACCEPT_VALUE);
        headers.putSingle(SESSION_ID, SESSION_ID_VALUE);
        headers.putSingle(USER_ID, USER_ID_VALUE);

        when(context.getHeaders()).thenReturn(headers);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(SERVICE_CONTEXT_NAME_VALUE);
        when(context.getMediaType()).thenReturn(new MediaType("application/test", "+json", "UTF-8"));
        when(context.getEntityStream()).thenReturn(toInputStream(jsonEnvelope.toDebugStringPrettyPrint(), "UTF-8"));

        loggerRequestDataFilter.filter(context);

        assertThat(MDC.get(REQUEST_DATA), isJson(
                allOf(
                        withJsonPath("$." + METADATA + ".id", equalTo(MESSAGE_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".correlation.client", equalTo(CLIENT_CORRELATION_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".context.session", equalTo(SESSION_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".name", equalTo(NAME_VALUE)),
                        withJsonPath("$." + METADATA + ".context.user", equalTo(USER_ID_VALUE)),
                        withJsonPath("$." + CONTENT_TYPE, equalTo(CONTENT_TYPE_VALUE)),
                        withJsonPath("$." + ACCEPT, equalTo(ACCEPT_VALUE)),
                        withJsonPath("$." + SERVICE_CONTEXT, equalTo(SERVICE_CONTEXT_NAME_VALUE))))
        );
    }

    @Test
    @SuppressWarnings({"unchecked", "deprecation"})
    public void shouldMergeDataInHeadersWithPayloadMetadataOnlyIfPresent() throws Exception {
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(MESSAGE_ID_VALUE, NAME_VALUE)
                        .withClientCorrelationId(CLIENT_CORRELATION_ID_VALUE))
                .withPayloadOf("data", "someData")
                .build();

        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        headers.putSingle(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        headers.putSingle(ACCEPT, ACCEPT_VALUE);

        when(context.getHeaders()).thenReturn(headers);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn(SERVICE_CONTEXT_NAME_VALUE);
        when(context.getMediaType()).thenReturn(new MediaType("application/test", "+json", "UTF-8"));
        when(context.getEntityStream()).thenReturn(toInputStream(jsonEnvelope.toDebugStringPrettyPrint(), "UTF-8"));

        loggerRequestDataFilter.filter(context);

        assertThat(MDC.get(REQUEST_DATA), isJson(
                allOf(
                        withJsonPath("$." + METADATA + ".id", equalTo(MESSAGE_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".correlation.client", equalTo(CLIENT_CORRELATION_ID_VALUE)),
                        withJsonPath("$." + METADATA + ".name", equalTo(NAME_VALUE)),
                        withJsonPath("$." + CONTENT_TYPE, equalTo(CONTENT_TYPE_VALUE)),
                        withJsonPath("$." + ACCEPT, equalTo(ACCEPT_VALUE)),
                        withJsonPath("$." + SERVICE_CONTEXT, equalTo(SERVICE_CONTEXT_NAME_VALUE)),
                        hasNoJsonPath("$." + METADATA + ".context.session"),
                        hasNoJsonPath("$." + METADATA + ".context.user")
                )));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnEmptyJsonIfNoValuesForCommand() throws Exception {
        final MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);

        when(context.getMediaType()).thenReturn(new MediaType("", ""));
        when(context.getHeaders()).thenReturn(headers);

        loggerRequestDataFilter.filter(context);

        assertThat(MDC.get(REQUEST_DATA), is("{}"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnEmptyJsonIfPayloadIsEmpty() throws Exception {
        final MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);

        when(context.getHeaders()).thenReturn(headers);
        when(context.getMediaType()).thenReturn(new MediaType("application/test", "+json", "UTF-8"));
        when(context.getEntityStream()).thenReturn(toInputStream("", "UTF-8"));

        loggerRequestDataFilter.filter(context);

        assertThat(MDC.get(REQUEST_DATA), is("{}"));
    }

    @Test
    @SuppressWarnings({"unchecked", "deprecation"})
    public void shouldIgnoreSubFieldCalledMetadata() throws Exception {
        final JsonObject payload = createObjectBuilder()
                .add("toplevel", createObjectBuilder()
                        .add("_metadata", JsonValue.NULL)
                ).build();

        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();

        when(context.getHeaders()).thenReturn(headers);
        when(context.getMediaType()).thenReturn(new MediaType("application/test", "+json", "UTF-8"));
        when(context.getEntityStream()).thenReturn(toInputStream(payload.toString(), "UTF-8"));

        loggerRequestDataFilter.filter(context);

        assertThat(MDC.get(REQUEST_DATA), is("{}"));

        verify(context).setEntityStream(Mockito.any(ByteArrayInputStream.class));
    }

}
