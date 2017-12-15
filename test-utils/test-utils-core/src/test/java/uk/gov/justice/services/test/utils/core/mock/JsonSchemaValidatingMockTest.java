package uk.gov.justice.services.test.utils.core.mock;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.envelope.EnvelopeValidationException;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaValidatingMockTest {

    @Test
    public void shouldPassWhenPayloadPassedToSenderAdheresToSchema() throws Exception {

        new SendingHandler(mock(Sender.class)).handle(
                envelope()
                        .with(metadataWithRandomUUID("example.add-recipe"))
                        .withPayloadOf("someName", "name")
                        .withPayloadOf(true, "glutenFree")
                        .build());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenPayloadPassedToSenderDoesNotAdhereToSchema() {

        exception.expect(MockitoException.class);
        exception.expectCause(allOf(instanceOf(EnvelopeValidationException.class),
                hasProperty("message", containsString("Message not valid against schema"))));

        new SendingHandler(mock(Sender.class)).handle(
                envelope()
                        .with(metadataWithRandomUUID("example.add-recipe"))
                        .withPayloadOf(true, "glutenFree")
                        .build());
    }

    @Test
    public void shouldThrowExceptionIfNoMetadataInEnvelope() {

        exception.expect(MockitoException.class);
        exception.expectCause(allOf(instanceOf(EnvelopeValidationException.class),
                hasProperty("message", equalTo("Metadata not set in the envelope."))));

        new SendingHandler(mock(Sender.class)).handle(
                envelope()
                        .withPayloadOf("someName", "name")
                        .withPayloadOf(true, "glutenFree")
                        .build());
    }

    @Test
    public void shouldPassWhenPayloadReturnedByRequesterAdheresToSchema() throws Exception {

        final Requester requester = mock(Requester.class);

        when(requester.request(any(JsonEnvelope.class))).thenReturn(envelope()
                .with(metadataWithRandomUUID("example.get-recipe"))
                .withPayloadOf("someName", "name")
                .withPayloadOf(true, "glutenFree")
                .build());

        new RequestingHandler(requester).handle(envelope().build());
    }

    @Test
    public void shouldThrowExceptionWhenPayloadReturnedByRequesterDoesNotAdhereToSchema() {

        exception.expect(MockitoException.class);
        exception.expectCause(allOf(instanceOf(EnvelopeValidationException.class),
                hasProperty("message", containsString("Message not valid against schema"))));

        final Requester requester = mock(Requester.class);

        when(requester.request(any(JsonEnvelope.class))).thenReturn(envelope()
                .with(metadataWithRandomUUID("example.get-recipe"))
                .withPayloadOf("someName", "name")
                .build());

        new RequestingHandler(requester).handle(envelope().build());
    }

    @Test
    public void shouldSkipValidationIfSkippingListenerAddedToConfig() {

        new SendingHandler(
                mock(Sender.class, withSettings().invocationListeners(new SkipJsonValidationListener())))
                .handle(envelope()
                        .with(metadataWithRandomUUID("unknown"))
                        .build());
    }

    @Test
    public void shouldSkipValidationOnOtherInterface() throws Exception {

        new OtherHandler(mock(SomeInterface.class)).handle(
                envelope()
                        .with(metadataWithDefaults())
                        .withPayloadOf(true, "glutenFree")
                        .build());
    }

    public static class SendingHandler {
        private Sender sender;

        public SendingHandler(final Sender sender) {
            this.sender = sender;
        }

        public void handle(final JsonEnvelope envelope) {
            sender.send(envelope);
        }
    }

    public static class RequestingHandler {
        private Requester requester;

        public RequestingHandler(final Requester requester) {
            this.requester = requester;
        }

        public JsonEnvelope handle(final JsonEnvelope envelope) {
            return requester.request(envelope);
        }
    }

    public static class OtherHandler {
        private SomeInterface someInterface;

        public OtherHandler(final SomeInterface someInterface) {
            this.someInterface = someInterface;
        }

        @Handles("example.add-recipe")
        public void handle(final JsonEnvelope envelope) {
            someInterface.process(envelope);
        }
    }
}
