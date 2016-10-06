package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.JsonObject;

import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches a JsonEnvelope Metadata and Payload.  This can be used independently or with {@link
 * JsonEnvelopeStreamMatcher} and {@link JsonEnvelopeListMatcher}.
 *
 * <pre>
 *  {@code
 *         assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
 *                              metadata()
 *                                  .withName("event.action"),
 *                              payLoad().isJson(allOf(
 *                                  withJsonPath("$.someId", equalTo(ID.toString())),
 *                                  withJsonPath("$.name", equalTo(NAME)))
 *                              )));
 * }
 * </pre>
 *
 * This makes use of {@link IsJson} to achieve Json matching in the payload.
 */
public class JsonEnvelopeMatcher extends TypeSafeDiagnosingMatcher<JsonEnvelope> {

    private final JsonEnvelopeMetadataMatcher metadataMatcher;
    private final JsonEnvelopePayloadMatcher payloadMatcher;

    public JsonEnvelopeMatcher(final JsonEnvelopeMetadataMatcher metadataMatcher, final JsonEnvelopePayloadMatcher payloadMatcher) {
        this.metadataMatcher = metadataMatcher;
        this.payloadMatcher = payloadMatcher;
    }

    public static JsonEnvelopeMatcher jsonEnvelope(final JsonEnvelopeMetadataMatcher metadataMatcher, final JsonEnvelopePayloadMatcher payloadMatcher) {
        return new JsonEnvelopeMatcher(metadataMatcher, payloadMatcher);
    }

    @Override
    public void describeTo(final Description description) {
        description
                .appendText("JsonEnvelope that contains (")
                .appendDescriptionOf(metadataMatcher)
                .appendDescriptionOf(payloadMatcher)
                .appendText(") ");
    }

    @Override
    protected boolean matchesSafely(final JsonEnvelope jsonEnvelope, final Description description) {
        final Metadata metadata = jsonEnvelope.metadata();
        final JsonObject payload = jsonEnvelope.payloadAsJsonObject();

        if (!metadataMatcher.matches(metadata)) {
            metadataMatcher.describeMismatch(metadata, description);
            return false;
        }

        if (!payloadMatcher.matches(payload)) {
            payloadMatcher.describeMismatch(payload, description);
            return false;
        }

        return true;
    }
}