package uk.gov.justice.services.adapter.rest.processor.response;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AcceptedStatusNoEntityResponseStrategyTest {

    @InjectMocks
    private AcceptedStatusNoEntityResponseStrategy acceptedStatusNoEntityResponseStrategy;

    @Test
    public void shouldReturnAcceptedStatusWithNoEntity() throws Exception {
        final UUID id = randomUUID();
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, "Name"))
                .withPayloadOf("payload", "payload")
                .build();

        final Response response = acceptedStatusNoEntityResponseStrategy
                .responseFor("action.name", Optional.of(jsonEnvelope));

        assertThat(response.getStatus(), equalTo(ACCEPTED.getStatusCode()));

        final Object entity = response.getEntity();
        assertThat(entity, is(nullValue()));
    }
}