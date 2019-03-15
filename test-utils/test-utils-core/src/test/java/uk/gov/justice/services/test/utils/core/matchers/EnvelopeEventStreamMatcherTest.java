//package uk.gov.justice.services.test.utils.core.matchers;
//
//import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
//import static java.util.UUID.randomUUID;
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.Matchers.allOf;
//import static org.junit.Assert.assertThat;
//import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeEventStreamMatcher.eventStreamAppendedAfter;
//import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeEventStreamMatcher.eventStreamAppendedWith;
//import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeEventStreamMatcher.eventStreamWithEmptyStream;
//import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeMatcher.envelope;
//import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeMetadataMatcher.metadata;
//import static uk.gov.justice.services.test.utils.core.matchers.EnvelopePayloadMatcher.payloadIsJson;
//import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeStreamMatcher.streamContaining;
//
//import uk.gov.justice.services.eventsourcing.source.core.EventStream;
//import uk.gov.justice.services.messaging.Envelope;
//import uk.gov.justice.services.messaging.JsonEnvelope;
//import uk.gov.justice.services.messaging.Metadata;
//import uk.gov.justice.services.messaging.spi.DefaultJsonMetadata;
//
//import java.util.UUID;
//import java.util.stream.Stream;
//
//import javax.json.Json;
//import javax.json.JsonObject;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//
//@RunWith(MockitoJUnitRunner.class)
//public class EnvelopeEventStreamMatcherTest {
//
//
//    private static final UUID ID_1 = randomUUID();
//    private static final UUID ID_2 = randomUUID();
//    private static final String NAME_1 = "someName_1";
//    private static final String NAME_2 = "someName_2";
//    private static final long VERSION = 3L;
//
//    @Mock
//    private EventStream eventStream;
//
//    @Test
//    public void shouldMatchJsonEnvelopeStreamAppendedToEventStream() throws Exception {
//        final Envelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
//        final Envelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);
//
//        eventStream.append(Stream.of(event_1, event_2), JsonEnvelope.class);
//
//        assertThat(eventStream, eventStreamAppendedWith(
//                streamContaining(
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_1)))
//                                )),
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_2.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_2)))
//                                ))
//                )));
//    }
//
//    @Test(expected = AssertionError.class)
//    public void shouldNotMatchJsonEnvelopesStreamAppendedToEventStreamIfJsonEnvelopeIsMissing() throws Exception {
//        final Envelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
//        final Envelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);
//
//        eventStream.append(Stream.of(event_1, event_2), JsonEnvelope.class);
//
//        assertThat(eventStream, eventStreamAppendedWith(
//                streamContaining(
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_1)))
//                                ))
//                )));
//    }
//
//    @Test(expected = AssertionError.class)
//    public void shouldNotMatchIfAppendNotCalled() throws Exception {
//        assertThat(eventStream, eventStreamAppendedWith(
//                streamContaining(
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_1)))
//                                ))
//                )));
//    }
//
//    @Test
//    public void shouldMatchJsonEnvelopeStreamAppendedAfterVersionOfEventStream() throws Exception {
//        final Envelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
//        final Envelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);
//
//        eventStream.appendAfter(Stream.of(event_1, event_2), VERSION, JsonEnvelope.class);
//
//        assertThat(eventStream, eventStreamAppendedAfter(VERSION).with(
//                streamContaining(
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_1)))
//                                )),
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_2.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_2)))
//                                ))
//                )));
//    }
//
//    @Test(expected = AssertionError.class)
//    public void shouldNotMatchJsonEnvelopeStreamAppendedAfterVersionOfEventStreamIfJsonEnvelopeIsMissing() throws Exception {
//        final Envelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
//        final Envelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);
//
//        eventStream.appendAfter(Stream.of(event_1, event_2), VERSION, JsonEnvelope.class);
//
//        assertThat(eventStream, eventStreamAppendedAfter(VERSION).with(
//                streamContaining(
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_1)))
//                                ))
//                )));
//    }
//
//    @Test(expected = AssertionError.class)
//    public void shouldNotMatchIfAppendAfterNotCalled() throws Exception {
//        assertThat(eventStream, eventStreamAppendedAfter(VERSION).with(
//                streamContaining(
//                        envelope(
//                                metadata().withName("event.action"),
//                                payloadIsJson(allOf(
//                                        withJsonPath("$.someId", equalTo(ID_1.toString())),
//                                        withJsonPath("$.name", equalTo(NAME_1)))
//                                ))
//                )));
//    }
//
//    @Test
//    public void shouldMatchAnEmptyStream() throws Exception {
//        eventStream.append(Stream.empty(), JsonEnvelope.class);
//        assertThat(eventStream, eventStreamWithEmptyStream());
//    }
//
//    @Test(expected = AssertionError.class)
//    public void shouldNotMatchANonEmptyStream() throws Exception {
//        final Envelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
//        eventStream.append(Stream.of(event_1), JsonEnvelope.class);
//
//        assertThat(eventStream, eventStreamWithEmptyStream());
//    }
//
//    private Envelope jsonEnvelopeWith(final UUID id, final String name) {
//        Metadata metadata = DefaultJsonMetadata.metadataBuilder().withId(randomUUID()).withName("event.action").build();
//        JsonObject payload = Json.createObjectBuilder().add("someId", id.toString()).add("name", name).build();
//        return Envelope.envelopeFrom(metadata, payload);
//    }
//}