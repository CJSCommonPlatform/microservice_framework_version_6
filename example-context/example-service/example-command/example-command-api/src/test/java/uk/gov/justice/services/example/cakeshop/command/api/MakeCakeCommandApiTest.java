package uk.gov.justice.services.example.cakeshop.command.api;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MakeCakeCommandApiTest {

    @Mock
    private Sender sender;

    @InjectMocks
    private MakeCakeCommandApi commandApi;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeCaptor;

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        assertThat(MakeCakeCommandApi.class, isHandlerClass(COMMAND_API)
                .with(method("handle")
                        .thatHandles("example.make-cake")));
    }

    @Test
    public void shouldHandleMakeCakeRequest() {
        commandApi = new MakeCakeCommandApi();
        commandApi.sender = sender;

        final JsonEnvelope envelope = envelope()
                .with(metadataWithDefaults().withName("example.make-cake"))
                .withPayloadOf("Field", "Value").build();

        final Envelope<JsonObject> jsonObjectEnvelope = commandApi.handle(envelope);

        assertThat(jsonObjectEnvelope.payload().getString("status"), equalTo("Making Cake"));

        verify(sender).send(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().metadata().name(), is("example.command.make-cake"));
    }
}
