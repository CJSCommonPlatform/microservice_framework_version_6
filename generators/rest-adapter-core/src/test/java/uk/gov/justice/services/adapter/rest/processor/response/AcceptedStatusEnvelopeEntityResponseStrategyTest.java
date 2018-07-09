package uk.gov.justice.services.adapter.rest.processor.response;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AcceptedStatusEnvelopeEntityResponseStrategyTest {

    @Spy
    private ResponseStrategyHelper responseStrategyHelper;

    @InjectMocks
    private AcceptedStatusEnvelopeEntityResponseStrategy acceptedStatusEnvelopeEntityResponseStrategy;

    @Test
    public void shouldReturnAcceptedStatusWithEntity() {
        final UUID id = randomUUID();
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, "Name"))
                .withPayloadOf("payload", "payload_" + RandomStringUtils.random(10))
                .build();

        final Response response = acceptedStatusEnvelopeEntityResponseStrategy
                .responseFor("action.name", Optional.of(jsonEnvelope));

        assertThat(response.getStatus(), equalTo(ACCEPTED.getStatusCode()));

        final Object entity = response.getEntity();

        assertThat(entity, instanceOf(JsonObject.class));
        assertThat(entity, equalTo(jsonEnvelope.payload()));
    }

}