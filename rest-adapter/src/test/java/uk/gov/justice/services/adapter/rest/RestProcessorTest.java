package uk.gov.justice.services.adapter.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilder;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.Envelope;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link RestProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestProcessorTest {

    @Mock
    private Consumer<Envelope> consumer;

    @Mock
    private HttpHeaders headers;

    @Mock
    private Map<String, String> pathParams;

    @Mock
    private JsonObject payload;

    @Mock
    private RestEnvelopeBuilderFactory envelopeBuilderFactory;

    @Mock
    private RestEnvelopeBuilder envelopeBuilder;

    @Mock
    private Envelope envelope;

    private RestProcessor restProcessor;

    @Before
    public void setup() {
        restProcessor = new RestProcessor();
        restProcessor.envelopeBuilderFactory = envelopeBuilderFactory;
        when(envelopeBuilderFactory.builder()).thenReturn(envelopeBuilder);
        when(envelopeBuilder.withHeaders(headers)).thenReturn(envelopeBuilder);
        when(envelopeBuilder.withInitialPayload(payload)).thenReturn(envelopeBuilder);
        when(envelopeBuilder.withPathParams(pathParams)).thenReturn(envelopeBuilder);
        when(envelopeBuilder.build()).thenReturn(envelope);
    }

    @Test
    public void shouldReturn202Response() throws Exception {
        Response response = restProcessor.process(consumer, payload, headers, pathParams);

        assertThat(response.getStatus(), equalTo(202));
    }

    @Test
    public void shouldCallConsumer() throws Exception {
        restProcessor.process(consumer, payload, headers, pathParams);

        verify(consumer).accept(envelope);
    }

    @Test
    public void shouldBuildCorrectEnvelope() throws Exception {
        restProcessor.process(consumer, payload, headers, pathParams);

        verify(envelopeBuilder).withInitialPayload(payload);
        verify(envelopeBuilder).withHeaders(headers);
        verify(envelopeBuilder).withPathParams(pathParams);
    }
}
