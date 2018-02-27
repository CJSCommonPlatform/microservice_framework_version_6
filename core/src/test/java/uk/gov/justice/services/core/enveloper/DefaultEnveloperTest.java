package uk.gov.justice.services.core.enveloper;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.enveloper.exception.InvalidEventException;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEnveloperTest {

    private static final String TEST_EVENT_NAME = "test.event.something-happened";
    private static final UUID COMMAND_UUID = randomUUID();
    private static final UUID OLD_CAUSATION_ID = randomUUID();
    private static final String TEST_NAME = "test.query.query-response";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private DefaultEnveloper enveloper;

    @Before
    public void setup() throws Exception {
        enveloper = new DefaultEnveloper(
                new UtcClock(),
                new ObjectToJsonValueConverter(new ObjectMapperProducer().objectMapper()));
    }

    @Test
    public void shouldEnvelopeEventObject() throws Exception {
        enveloper.register(new EventFoundEvent(TestEvent.class, TEST_EVENT_NAME));

        final JsonEnvelope event = enveloper.withMetadataFrom(
                envelopeFrom(
                        metadataBuilder()
                                .withId(COMMAND_UUID)
                                .withName(TEST_EVENT_NAME)
                                .withCausation(OLD_CAUSATION_ID),
                        createObjectBuilder()))
                .apply(new TestEvent("somePayloadValue"));

        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().id(), not(equalTo(COMMAND_UUID)));
        assertThat(event.metadata().name(), equalTo(TEST_EVENT_NAME));
        assertThat(event.metadata().causation().size(), equalTo(2));
        assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
        assertThat(event.payloadAsJsonObject().getString("somePayloadKey"), equalTo("somePayloadValue"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullEvent() throws Exception {
        enveloper.withMetadataFrom(envelopeFrom(metadataBuilder().withId(randomUUID()).withName("name"), createObjectBuilder())).apply(null);
    }

    @Test
    public void shouldEnvelopeObjectWithName() throws Exception {

        final JsonEnvelope event = enveloper.withMetadataFrom(
                envelopeFrom(
                        metadataBuilder()
                                .withId(COMMAND_UUID)
                                .withName(TEST_EVENT_NAME)
                                .withCausation(OLD_CAUSATION_ID),
                        createObjectBuilder()), TEST_NAME)
                .apply(new TestEvent());


        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_NAME));
        assertThat(event.metadata().causation().size(), equalTo(2));
        assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
    }

    @Test
    public void shouldEnvelopeMapNullObjectWithName() throws Exception {

        final JsonEnvelope event = enveloper.withMetadataFrom(
                envelopeFrom(
                        metadataBuilder()
                                .withId(COMMAND_UUID)
                                .withName(TEST_EVENT_NAME)
                                .withCausation(OLD_CAUSATION_ID),
                        createObjectBuilder()), TEST_NAME)
                .apply(null);

        assertThat(event.payload(), equalTo(JsonValue.NULL));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_NAME));
        assertThat(event.metadata().causation().size(), equalTo(2));
        assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
    }

    @Test
    public void shouldEnvelopeObjectWithoutCausation() throws Exception {
        enveloper.register(new EventFoundEvent(TestEvent.class, TEST_EVENT_NAME));

        final JsonEnvelope event = enveloper.withMetadataFrom(
                envelopeFrom(
                        metadataBuilder()
                                .withId(COMMAND_UUID)
                                .withName(TEST_EVENT_NAME),
                        createObjectBuilder()))
                .apply(new TestEvent());


        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_EVENT_NAME));
        assertThat(event.metadata().causation().size(), equalTo(1));
        assertThat(event.metadata().causation().get(0), equalTo(COMMAND_UUID));

    }

    @Test
    public void shouldThrowExceptionIfProvidedInvalidEventObject() {
        exception.expect(InvalidEventException.class);
        exception.expectMessage("Failed to map event. No event registered for class java.lang.String");

        enveloper.withMetadataFrom(envelopeFrom(metadataBuilder().withId(randomUUID()).withName("name"), createObjectBuilder())).apply("InvalidEventObject");
    }

    @Test
    public void shouldRemoveStreamMetadata() throws Exception {
        enveloper.register(new EventFoundEvent(TestEvent.class, TEST_EVENT_NAME));

        final JsonEnvelope event = enveloper.withMetadataFrom(
                envelopeFrom(
                        metadataBuilder()
                                .withId(COMMAND_UUID)
                                .withName(TEST_EVENT_NAME)
                                .withStreamId(randomUUID())
                                .withVersion(123l),
                        createObjectBuilder()))
                .apply(new TestEvent());

        assertThat(event.metadata().streamId(), is(empty()));
        assertThat(event.metadata().version(), is(empty()));
    }

    @Test
    public void shouldRemoveStreamMetadataWithName() throws Exception {
        enveloper.register(new EventFoundEvent(TestEvent.class, TEST_EVENT_NAME));

        final JsonEnvelope event = enveloper.withMetadataFrom(
                envelopeFrom(
                        metadataBuilder()
                                .withId(COMMAND_UUID)
                                .withName(TEST_EVENT_NAME)
                                .withStreamId(randomUUID())
                                .withVersion(123l),
                        createObjectBuilder()), "new.name")
                .apply(new TestEvent());

        assertThat(event.metadata().streamId(), is(empty()));
        assertThat(event.metadata().version(), is(empty()));
    }

    @Event("Test-Event")
    public static class TestEvent {
        private String somePayloadKey;

        public TestEvent(final String somePayloadKey) {
            this.somePayloadKey = somePayloadKey;
        }

        public TestEvent() {
        }

        public String getSomePayloadKey() {
            return somePayloadKey;
        }
    }
}
