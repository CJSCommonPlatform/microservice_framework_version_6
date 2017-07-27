package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;

public class JsonEnvelopeStreamMatcherTest {

    private static final UUID ID_1 = randomUUID();
    private static final UUID ID_2 = randomUUID();
    private static final String NAME_1 = "someName_1";
    private static final String NAME_2 = "someName_2";

    @Test
    public void shouldMatchJsonEnvelopesInAStream() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        assertThat(Stream.of(event_1, event_2), JsonEnvelopeStreamMatcher.streamContaining(
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
        ));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopesInAStreamIfThereAreTooMany() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        assertThat(Stream.of(event_1, event_2), JsonEnvelopeStreamMatcher.streamContaining(
                jsonEnvelope(
                        metadata().withName("event.action"),
                        payloadIsJson(allOf(
                                withJsonPath("$.someId", equalTo(ID_1.toString())),
                                withJsonPath("$.name", equalTo(NAME_1)))
                        ))
        ));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopesInAStreamIfOneIsMissing() throws Exception {
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        assertThat(Stream.of(event_2), JsonEnvelopeStreamMatcher.streamContaining(
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
        ));
    }

    private JsonEnvelope jsonEnvelopeWith(final UUID id, final String name) {
        return envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf(id.toString(), "someId")
                .withPayloadOf(name, "name")
                .build();
    }
}