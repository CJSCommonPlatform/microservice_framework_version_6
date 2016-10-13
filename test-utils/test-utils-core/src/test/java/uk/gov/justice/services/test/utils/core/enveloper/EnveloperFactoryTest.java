package uk.gov.justice.services.test.utils.core.enveloper;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeListMatcher.listContaining;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
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

        assertThat(result, jsonEnvelope(
                metadata().withName("expected.name"),
                payloadIsJson(allOf(
                        withJsonPath("$.name", equalTo("output")),
                        withoutJsonPath("$.value"))
                )));
    }

    @Test
    public void shouldCreateEnveloperWithRegisteredEventClasses() throws Exception {
        final UUID id1 = UUID.randomUUID();
        final String name = "name";
        final String id2 = "id2";
        final String value = "value";

        final JsonObject inputPayload = Json.createObjectBuilder().add(value, "init").build();
        final JsonEnvelope envelope = EnvelopeFactory.createEnvelope("init.name", inputPayload);

        final EnveloperFactoryTest.TestEvent1 testEvent_1 = new EnveloperFactoryTest.TestEvent1(id1, name);
        final EnveloperFactoryTest.TestEvent2 testEvent_2 = new EnveloperFactoryTest.TestEvent2(id2, value);
        final Stream<Object> events = Stream.of(testEvent_1, testEvent_2);

        final Enveloper enveloper = EnveloperFactory.createEnveloperWithEvents(TestEvent1.class, TestEvent2.class);

        final List<JsonEnvelope> resultEvents = events.map(enveloper.withMetadataFrom(envelope)).collect(toList());

        assertThat(resultEvents, listContaining(
                jsonEnvelope(
                        metadata().withName("test.event.1"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", equalTo(id1.toString())),
                                withJsonPath("$.name", equalTo(name)))
                        )),
                jsonEnvelope(
                        metadata().withName("test.event.2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", equalTo(id2)),
                                withJsonPath("$.value", equalTo(value)))
                        ))
        ));
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