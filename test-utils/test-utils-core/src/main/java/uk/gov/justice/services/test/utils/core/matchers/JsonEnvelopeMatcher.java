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
 * Where the test should specify the metadata use the 'metadata' method. For example:
 * <pre>
 *  {@code
 *         assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
 *                              metadata()
 *                                  .withUserId(userId)
 *                                  .withName("event.action"),
 *                              payloadIsJson(allOf(
 *                                  withJsonPath("$.someId", equalTo(ID.toString())),
 *                                  withJsonPath("$.name", equalTo(NAME)))
 *                              )));
 * }
 * </pre>
 *
 * Where expected JsonEnvelope is enveloped using the input JsonEnvelope you can use
 * 'withMetadataEnvelopedFrom' and provide the input JsonEnvelope to match. For example:
 * <pre>
 *  {@code
 *         assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
 *                              withMetadataEnvelopedFrom(commandJsonEnvelope)
 *                                  .withName("event.action"),
 *                              payloadIsJson(allOf(
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