package uk.gov.justice.services.test.utils.core.matchers;

import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isValidJsonEnvelopeForSchema;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import javax.json.JsonValue;

import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches an {@code Envelope<JsonValue>} Metadata and Payload.  This can be used independently or
 * with {@link JsonEnvelopeStreamMatcher} and {@link JsonEnvelopeListMatcher}.
 *
 * Where the test should specify the metadata use the 'metadata' method. For example:
 * <pre>
 *  {@code
 *         assertThat(jsonValueEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope(
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
 *         assertThat(jsonValueEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope(
 *                              withMetadataEnvelopedFrom(commandJsonEnvelope)
 *                                  .withName("event.action"),
 *                              payloadIsJson(allOf(
 *                                  withJsonPath("$.someId", equalTo(ID.toString())),
 *                                  withJsonPath("$.name", equalTo(NAME)))
 *                              )));
 * }
 * </pre>
 *
 * Where expected JsonEnvelope has a schema matching the Metadata name field and is on the classpath
 * at 'raml/json/schema/event.action.json' The JsonEnvelope can be validated as follows:
 * <pre>
 *  {@code
 *         assertThat(jsonValueEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope(
 *                              withMetadataEnvelopedFrom(commandJsonEnvelope)
 *                                  .withName("event.action"),
 *                              payloadIsJson(allOf(
 *                                  withJsonPath("$.someId", equalTo(ID.toString())),
 *                                  withJsonPath("$.name", equalTo(NAME)))))
 *                              .thatMatchesSchema());
 * }
 * </pre>
 *
 * Where expected envelope payload is a JsonValue.NULL:
 * <pre>
 *  {@code
 *         assertThat(jsonValueEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope(
 *                              withMetadataEnvelopedFrom(commandJsonEnvelope)
 *                                  .withName("event.action"),
 *                              .withPayloadOf(payloadIsJsonValueNull()));
 * }
 * </pre>
 *
 * This makes use of {@link IsJson} to achieve Json matching in the payload.
 */
public class JsonValueEnvelopeMatcher extends TypeSafeDiagnosingMatcher<Envelope<JsonValue>> {

    private Optional<JsonEnvelopeMetadataMatcher> jsonEnvelopeMetadataMatcher = Optional.empty();
    private Optional<JsonEnvelopePayloadMatcher> jsonEnvelopePayloadMatcher = Optional.empty();
    private Optional<Matcher> schemaMatcher = Optional.empty();

    public static JsonValueEnvelopeMatcher jsonValueEnvelope() {
        return new JsonValueEnvelopeMatcher();
    }

    public static JsonValueEnvelopeMatcher jsonValueEnvelope(final JsonEnvelopeMetadataMatcher jsonEnvelopeMetadataMatcher,
                                                             final JsonEnvelopePayloadMatcher jsonEnvelopePayloadMatcher) {
        return new JsonValueEnvelopeMatcher()
                .withMetadataOf(jsonEnvelopeMetadataMatcher)
                .withPayloadOf(jsonEnvelopePayloadMatcher);
    }

    /**
     * Validate the {@code Envelope<JsonValue>} metadata using {@link JsonEnvelopeMetadataMatcher}
     *
     * @param jsonEnvelopeMetadataMatcher the JsonEnvelopeMetadataMatcher to use
     * @return the matcher instance
     */
    public JsonValueEnvelopeMatcher withMetadataOf(final JsonEnvelopeMetadataMatcher jsonEnvelopeMetadataMatcher) {
        this.jsonEnvelopeMetadataMatcher = Optional.of(jsonEnvelopeMetadataMatcher);
        return this;
    }

    /**
     * Validate the {@code Envelope<JsonValue>} payload using {@link JsonEnvelopePayloadMatcher}
     *
     * @param jsonEnvelopePayloadMatcher the JsonEnvelopePayloadMatcher to use
     * @return the matcher instance
     */
    public JsonValueEnvelopeMatcher withPayloadOf(final JsonEnvelopePayloadMatcher jsonEnvelopePayloadMatcher) {
        this.jsonEnvelopePayloadMatcher = Optional.of(jsonEnvelopePayloadMatcher);
        return this;
    }

    /**
     * Validates an {@code Envelope<JsonValue>} against the correct schema for the action name
     * provided in the metadata. Expects to find the schema on the class path in package
     * 'raml/json/schema/{action.name}.json'.
     *
     * @return the matcher instance
     */
    public JsonValueEnvelopeMatcher thatMatchesSchema() {
        this.schemaMatcher = Optional.of(isValidJsonEnvelopeForSchema());
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope that contains (");
        jsonEnvelopeMetadataMatcher.ifPresent(description::appendDescriptionOf);
        jsonEnvelopePayloadMatcher.ifPresent(description::appendDescriptionOf);
        description.appendText(") ");
    }

    @Override
    protected boolean matchesSafely(final Envelope<JsonValue> envelope, final Description description) {
        final Metadata metadata = envelope.metadata();

        if (jsonEnvelopeMetadataMatcher.isPresent() && !jsonEnvelopeMetadataMatcher.get().matches(metadata)) {
            jsonEnvelopeMetadataMatcher.get().describeMismatch(metadata, description);
            return false;
        }

        if (jsonEnvelopePayloadMatcher.isPresent()) {
            final JsonValue payload = envelope.payload();

            if (!jsonEnvelopePayloadMatcher.get().matches(payload)) {
                jsonEnvelopePayloadMatcher.get().describeMismatch(payload, description);
                return false;
            }
        }

        if (schemaMatcher.isPresent() && !schemaMatcher.get().matches(envelope)) {
            schemaMatcher.get().describeMismatch(envelope, description);
            return false;
        }

        return true;
    }
}
