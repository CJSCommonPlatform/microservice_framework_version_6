package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;

import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamMatcherTest {

    private static final UUID ID_1 = randomUUID();
    private static final UUID ID_2 = randomUUID();
    private static final String NAME_1 = "someName_1";
    private static final String NAME_2 = "someName_2";
    private static final long VERSION = 3L;

    @Mock
    private EventStream eventStream;

    @Test
    public void shouldMatchJsonEnvelopeStreamAppendedToEventStream() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        eventStream.append(Stream.of(event_1, event_2));

        assertThat(eventStream, EventStreamMatcher.eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
                                        withJsonPath("$.name", equalTo(NAME_1)))
                                )),
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_2.toString())),
                                        withJsonPath("$.name", equalTo(NAME_2)))
                                ))
                )));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopesStreamAppendedToEventStreamIfJsonEnvelopeIsMissing() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        eventStream.append(Stream.of(event_1, event_2));

        assertThat(eventStream, EventStreamMatcher.eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
                                        withJsonPath("$.name", equalTo(NAME_1)))
                                ))
                )));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfAppendNotCalled() throws Exception {
        assertThat(eventStream, EventStreamMatcher.eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
                                        withJsonPath("$.name", equalTo(NAME_1)))
                                ))
                )));
    }

    @Test
    public void shouldMatchJsonEnvelopeStreamAppendedAfterVersionOfEventStream() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        eventStream.appendAfter(Stream.of(event_1, event_2), VERSION);

        assertThat(eventStream, EventStreamMatcher.eventStreamAppendedAfter(VERSION).with(
                streamContaining(
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
                                        withJsonPath("$.name", equalTo(NAME_1)))
                                )),
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_2.toString())),
                                        withJsonPath("$.name", equalTo(NAME_2)))
                                ))
                )));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopeStreamAppendedAfterVersionOfEventStreamIfJsonEnvelopeIsMissing() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        eventStream.appendAfter(Stream.of(event_1, event_2), VERSION);

        assertThat(eventStream, EventStreamMatcher.eventStreamAppendedAfter(VERSION).with(
                streamContaining(
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
                                        withJsonPath("$.name", equalTo(NAME_1)))
                                ))
                )));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfAppendAfterNotCalled() throws Exception {
        assertThat(eventStream, EventStreamMatcher.eventStreamAppendedAfter(VERSION).with(
                streamContaining(
                        jsonEnvelope(
                                metadata().withName("event.action"),
                                payloadIsJson(allOf(
                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
                                        withJsonPath("$.name", equalTo(NAME_1)))
                                ))
                )));
    }

    @Test
    public void shouldMatchAnEmptyStream() throws Exception {
        eventStream.append(Stream.empty());
        assertThat(eventStream, EventStreamMatcher.eventStreamWithEmptyStream());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchANonEmptyStream() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        eventStream.append(Stream.of(event_1));

        assertThat(eventStream, EventStreamMatcher.eventStreamWithEmptyStream());
    }

    private JsonEnvelope jsonEnvelopeWith(final UUID id, final String name) {
        return envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf(id.toString(), "someId")
                .withPayloadOf(name, "name")
                .build();
    }
}
