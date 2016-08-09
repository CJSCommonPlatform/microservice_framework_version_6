package uk.gov.justice.services.core.audit;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.configuration.AppNameProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SimpleAuditClientTest {

    @Mock
    Logger logger;

    @Mock
    AppNameProvider appNameProvider;

    @InjectMocks
    private SimpleAuditClient simpleAuditClient;

    @Test
    public void shouldPrependTheAppNameToTheEnvelopeJsonAndLog() throws Exception {

        final String envelopeJson = new JSONObject()
                .put("name", "value")
                .toString();
        final String appName = "the-app-name";

        final JsonEnvelope envelope =  mock(JsonEnvelope.class);

        when(envelope.toString()).thenReturn(envelopeJson);
        when(appNameProvider.getAppName()).thenReturn(appName);

        simpleAuditClient.auditEntry(envelope);

        final ArgumentCaptor<String> argumentCaptor = forClass(String.class);

        verify(logger).info(argumentCaptor.capture());

        final String value = argumentCaptor.getValue();
        with(value)
                .assertEquals("appName", appName)
                .assertEquals("envelope.name", "value");
    }
}
