package uk.gov.justice.services.adapter.messaging;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.util.UUID;

import javax.interceptor.InvocationContext;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class JmsLoggerMetadataInterceptorTest {

    @Mock
    private Logger logger;

    @Mock
    private InvocationContext context;

    @Mock
    private JmsParameterChecker parameterChecker;

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @Mock
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Mock
    private TraceLogger traceLogger;

    @InjectMocks
    private JmsLoggerMetadataInterceptor jmsLoggerMetadataInterceptor;

    @Test
    @SuppressWarnings("deprecation")
    public void shouldAddMetadataFromEnvelopeToMappedDiagnosticContext() throws Exception {
        final UUID messageId = randomUUID();
        final String clientCorrelationId = randomUUID().toString();
        final String name = "someName";

        final JsonEnvelope jsonEnvelope =  envelopeFrom(metadataBuilder()
                        .withId(messageId)
                        .withName(name)
                        .withClientCorrelationId(clientCorrelationId),
                createObjectBuilder()
                        .add("data", "someData"));

        final TextMessage textMessage = mock(TextMessage.class);
        final JsonObject jsonObject = createObjectBuilder()
                .add("id", messageId.toString()).build();

        when(context.getParameters()).thenReturn(new Object[]{textMessage});
        when(textMessage.getText()).thenReturn(jsonEnvelope.toDebugStringPrettyPrint());

        when(jmsMessageLoggerHelper.metadataAsJsonObject(textMessage)).thenReturn(jsonObject);

        when(context.proceed()).thenAnswer(invocationOnMock -> {
            assertThat(MDC.get(REQUEST_DATA), isJson(
                    withJsonPath("$.metadata.id", equalTo(messageId.toString()))
            ));
            return null;
        });

        jmsLoggerMetadataInterceptor.addRequestDataToMappedDiagnosticContext(context);

        assertThat(MDC.get(REQUEST_DATA), nullValue());
    }

    @Test
    @SuppressWarnings({"unchecked", "deprecation"})
    public void shouldProceedWithContextAndReturnResult() throws Exception {
        final Object expectedResult = mock(Object.class);

        final JsonEnvelope jsonEnvelope =  envelopeFrom(metadataBuilder()
                        .withId(UUID.randomUUID())
                        .withName("someName"),
                createObjectBuilder()
                        .add("data", "someData"));

        final TextMessage textMessage = mock(TextMessage.class);

        when(context.getParameters()).thenReturn(new Object[]{textMessage});
        when(textMessage.getText()).thenReturn(jsonEnvelope.toDebugStringPrettyPrint());
        when(context.proceed()).thenReturn(expectedResult);

        final Object actualResult = jmsLoggerMetadataInterceptor.addRequestDataToMappedDiagnosticContext(context);

        assertThat(actualResult, is(expectedResult));
    }

    @Test
    @SuppressWarnings({"unchecked", "deprecation"})
    public void shouldReturnMessageInMetadataIfExceptionThrownWhenAccessingTextMessage() throws Exception {
        final TextMessage textMessage = mock(TextMessage.class);

        when(context.getParameters()).thenReturn(new Object[]{textMessage});
        when(textMessage.getText()).thenThrow(new RuntimeException());
        when(context.proceed()).thenAnswer(invocationOnMock -> {
            assertThat(MDC.get(REQUEST_DATA), isJson(
                    withJsonPath("$.metadata", equalTo("Could not find: _metadata in message"))
            ));
            return null;
        });

        jmsLoggerMetadataInterceptor.addRequestDataToMappedDiagnosticContext(context);
    }
}
