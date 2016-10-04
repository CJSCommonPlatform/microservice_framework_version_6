package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payLoad;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class JsonEnvelopeListMatcherTest {

    private static final UUID ID_1 = randomUUID();
    private static final UUID ID_2 = randomUUID();
    private static final String NAME_1 = "someName_1";
    private static final String NAME_2 = "someName_2";

    @Test
    public void shouldMatchJsonEnvelopesInAList() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        final List<JsonEnvelope> events = asList(event_1, event_2);

        assertThat(events, JsonEnvelopeListMatcher.listContaining(
                jsonEnvelope(
                        metadata().withName("event.action"),
                        payLoad().isJson(allOf(
                                withJsonPath("$.someId", equalTo(ID_1.toString())),
                                withJsonPath("$.name", equalTo(NAME_1)))
                        )),
                jsonEnvelope(
                        metadata().withName("event.action"),
                        payLoad().isJson(allOf(
                                withJsonPath("$.someId", equalTo(ID_2.toString())),
                                withJsonPath("$.name", equalTo(NAME_2)))
                        ))
        ));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopesInAListIfThereAreTooMany() throws Exception {
        final JsonEnvelope event_1 = jsonEnvelopeWith(ID_1, NAME_1);
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        final List<JsonEnvelope> events = asList(event_1, event_2);

        assertThat(events, JsonEnvelopeListMatcher.listContaining(
                jsonEnvelope(
                        metadata().withName("event.action"),
                        payLoad().isJson(allOf(
                                withJsonPath("$.someId", equalTo(ID_1.toString())),
                                withJsonPath("$.name", equalTo(NAME_1)))
                        ))
        ));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopesInAListIfOneIsMissing() throws Exception {
        final JsonEnvelope event_2 = jsonEnvelopeWith(ID_2, NAME_2);

        final List<JsonEnvelope> events = singletonList(event_2);

        assertThat(events, JsonEnvelopeListMatcher.listContaining(
                jsonEnvelope(
                        metadata().withName("event.action"),
                        payLoad().isJson(allOf(
                                withJsonPath("$.someId", equalTo(ID_1.toString())),
                                withJsonPath("$.name", equalTo(NAME_1)))
                        )),
                jsonEnvelope(
                        metadata().withName("event.action"),
                        payLoad().isJson(allOf(
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