package uk.gov.justice.services.adapter.rest.processor;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PayloadResponseFactoryTest {

    @Mock
    private Function<JsonEnvelope, Response> function;

    @Spy
    private ResponseFactoryHelper responseFactoryHelper;

    @InjectMocks
    private PayloadResponseFactory responseFactory;

    @Test
    public void shouldReturnOkResponse() throws Exception {
        final UUID id = randomUUID();
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, "Name"))
                .withPayloadOf("payload", "payload")
                .build();

        when(function.apply(jsonEnvelope)).thenReturn(status(OK).build());

        final Response response = responseFactory.responseFor("action.name", Optional.of(jsonEnvelope));

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(response.getHeaderString(ID), is(id.toString()));

        final JsonObject entity = (JsonObject) response.getEntity();
        assertThat(entity.toString(), is("{\"payload\":\"payload\"}"));
    }
}