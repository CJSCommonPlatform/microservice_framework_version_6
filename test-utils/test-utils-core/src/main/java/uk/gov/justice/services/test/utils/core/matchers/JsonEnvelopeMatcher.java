package uk.gov.justice.services.test.utils.core.matchers;

import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaValidationMatcher.isValidJsonEnvelopeForSchema;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import javax.json.JsonValue;

import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
 * Where expected JsonEnvelope has a schema matching the Metadata name field and is on the classpath
 * at 'raml/json/schema/event.action.json' The JsonEnvelope can be validated as follows:
 * <pre>
 *  {@code
 *         assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
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
 *         assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
 *                              withMetadataEnvelopedFrom(commandJsonEnvelope)
 *                                  .withName("event.action"),
 *                              .withPayloadOf(payloadIsJsonValueNull()));
 * }
 * </pre>
 *
 * This makes use of {@link IsJson} to achieve Json matching in the payload.
 */
public class JsonEnvelopeMatcher extends TypeSafeDiagnosingMatcher<JsonEnvelope> {

    private Optional<JsonEnvelopeMetadataMatcher> metadataMatcher = Optional.empty();
    private Optional<JsonEnvelopePayloadMatcher> payloadMatcher = Optional.empty();
    private Optional<Matcher> schemaMatcher = Optional.empty();

    public static JsonEnvelopeMatcher jsonEnvelope() {
        return new JsonEnvelopeMatcher();
    }

    public static JsonEnvelopeMatcher jsonEnvelope(final JsonEnvelopeMetadataMatcher metadataMatcher, final JsonEnvelopePayloadMatcher payloadMatcher) {
        return new JsonEnvelopeMatcher()
                .withMetadataOf(metadataMatcher)
                .withPayloadOf(payloadMatcher);
    }

    /**
     * Validate the JsonEnvelope metadata using {@link JsonEnvelopeMetadataMatcher}
     *
     * @param metadataMatcher the JsonEnvelopeMetadataMatcher to use
     * @return the matcher instance
     */
    public JsonEnvelopeMatcher withMetadataOf(final JsonEnvelopeMetadataMatcher metadataMatcher) {
        this.metadataMatcher = Optional.of(metadataMatcher);
        return this;
    }

    /**
     * Validate the JsonEnvelope payload using {@link JsonEnvelopePayloadMatcher}
     *
     * @param payloadMatcher the JsonEnvelopePayloadMatcher to use
     * @return the matcher instance
     */
    public JsonEnvelopeMatcher withPayloadOf(final JsonEnvelopePayloadMatcher payloadMatcher) {
        this.payloadMatcher = Optional.of(payloadMatcher);
        return this;
    }

    /**
     * Validates a JsonEnvelope against the correct schema for the action name provided in the
     * metadata. Expects to find the schema on the class path in package
     * 'raml/json/schema/{action.name}.json'.
     *
     * @return the matcher instance
     */
    public JsonEnvelopeMatcher thatMatchesSchema() {
        this.schemaMatcher = Optional.of(isValidJsonEnvelopeForSchema());
        return this;
    }

    public JsonEnvelopeMatcher thatMatchesSchemaInComponent(final String component) {
        this.schemaMatcher = Optional.of(isValidJsonEnvelopeForSchema(component));
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope that contains (");
        metadataMatcher.ifPresent(description::appendDescriptionOf);
        payloadMatcher.ifPresent(description::appendDescriptionOf);
        description.appendText(") ");
    }

    @Override
    protected boolean matchesSafely(final JsonEnvelope jsonEnvelope, final Description description) {
        final Metadata metadata = jsonEnvelope.metadata();

        if (metadataMatcher.isPresent() && !metadataMatcher.get().matches(metadata)) {
            metadataMatcher.get().describeMismatch(metadata, description);
            return false;
        }

        if (payloadMatcher.isPresent()) {
            final JsonValue payload = jsonEnvelope.payload();

            if (!payloadMatcher.get().matches(payload)) {
                payloadMatcher.get().describeMismatch(payload, description);
                return false;
            }
        }

        if (schemaMatcher.isPresent() && !schemaMatcher.get().matches(jsonEnvelope)) {
            schemaMatcher.get().describeMismatch(jsonEnvelope, description);
            return false;
        }

        return true;
    }
}