package uk.gov.justice.services.test.utils.core.enveloper;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class EnveloperFactoryTest {

    @Test
    public void shouldCreateUsableEnveloper() throws Exception {
        final JsonObject inputPayload = Json.createObjectBuilder().add("value", "init").build();
        final JsonObject outputPayload = Json.createObjectBuilder().add("name", "output").build();
        final JsonEnvelope envelope = EnvelopeFactory.createEnvelope("init.name", inputPayload);

        final JsonEnvelope result = EnveloperFactory.createEnveloper().withMetadataFrom(envelope, "expected.name").apply(outputPayload);

        final JsonObject resultJson = result.payloadAsJsonObject();
        assertThat(result.metadata().name(), is("expected.name"));
        assertThat(getString(resultJson, "name"), is(Optional.of("output")));
        assertThat(getString(resultJson, "value"), is(Optional.empty()));
    }

    @Test
    public void shouldCreateEnveloperWithRegisteredEventClasses() throws Exception {
        final JsonObject inputPayload = Json.createObjectBuilder().add("value", "init").build();
        final JsonEnvelope envelope = EnvelopeFactory.createEnvelope("init.name", inputPayload);
        final UUID id_1 = UUID.randomUUID();
        final EnveloperFactoryTest.TestEvent1 testEvent_1 = new EnveloperFactoryTest.TestEvent1(id_1, "name");
        final EnveloperFactoryTest.TestEvent2 testEvent_2 = new EnveloperFactoryTest.TestEvent2("id2", "value");
        final Stream<Object> events = Stream.of(testEvent_1, testEvent_2);

        final Enveloper enveloper = EnveloperFactory.createEnveloperWithEvents(TestEvent1.class, TestEvent2.class);
        final List<JsonEnvelope> resultEvents = events.map(enveloper.withMetadataFrom(envelope)).collect(toList());

        assertThat(resultEvents.size(), is(2));

        final JsonEnvelope result_1 = resultEvents.get(0);
        final JsonObject resultJson_1 = result_1.payloadAsJsonObject();
        assertThat(result_1.metadata().name(), is("test.event.1"));
        assertThat(JsonObjects.getUUID(resultJson_1, "id"), is(Optional.of(id_1)));
        assertThat(getString(resultJson_1, "name"), is(Optional.of("name")));

        final JsonEnvelope result_2 = resultEvents.get(1);
        final JsonObject resultJson_2 = result_2.payloadAsJsonObject();
        assertThat(result_2.metadata().name(), is("test.event.2"));
        assertThat(getString(resultJson_2, "id"), is(Optional.of("id2")));
        assertThat(getString(resultJson_2, "value"), is(Optional.of("value")));
    }

    @Event("test.event.1")
    public class TestEvent1 {
        private final UUID id;
        private final String name;

        public TestEvent1(final UUID id, final String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Event("test.event.2")
    public class TestEvent2 {
        private final String id;
        private final String value;

        public TestEvent2(final String id, final String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }
}