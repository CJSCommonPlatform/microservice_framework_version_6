package uk.gov.justice.services.test.utils.core.matchers;

import static uk.gov.justice.services.test.utils.core.matchers.EnvelopeSchemaValidatorMatcher.isValidEnvelopeForSchema;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class EnvelopeMatcher<T> extends TypeSafeDiagnosingMatcher<Envelope<T>> {

    private Optional<EnvelopeMetadataMatcher> metadataMatcher = Optional.empty();
    private Optional<Matcher<?>> payloadMatcher = Optional.empty();
    private Optional<Matcher> schemaMatcher = Optional.empty();

    public static EnvelopeMatcher envelope() {
        return new EnvelopeMatcher();
    }

    public static EnvelopeMatcher envelope(final EnvelopeMetadataMatcher metadataMatcher, final Matcher<?> payloadMatcher) {
        return new EnvelopeMatcher()
                .withMetadataOf(metadataMatcher)
                .withPayloadOf(payloadMatcher);
    }

    /**
     * Validate the JsonEnvelope metadata using {@link JsonEnvelopeMetadataMatcher}
     *
     * @param metadataMatcher the JsonEnvelopeMetadataMatcher to use
     * @return the matcher instance
     */
    public EnvelopeMatcher withMetadataOf(final EnvelopeMetadataMatcher metadataMatcher) {
        this.metadataMatcher = Optional.of(metadataMatcher);
        return this;
    }

    /**
     * Validate the JsonEnvelope payload using {@link JsonEnvelopePayloadMatcher}
     *
     * @param payloadMatcher the JsonEnvelopePayloadMatcher to use
     * @return the matcher instance
     */
    public EnvelopeMatcher withPayloadOf(final Matcher<?> payloadMatcher) {
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
    public EnvelopeMatcher thatMatchesSchema() {
        this.schemaMatcher = Optional.of(isValidEnvelopeForSchema());
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
    protected boolean matchesSafely(final Envelope<T> envelope, final Description description) {
        final Metadata metadata = envelope.metadata();

        if (metadataMatcher.isPresent() && !metadataMatcher.get().matches(metadata)) {
            metadataMatcher.get().describeMismatch(metadata, description);
            return false;
        }

        if (payloadMatcher.isPresent()) {
            final T payload = envelope.payload();

            if (!payloadMatcher.get().matches(payload)) {
                payloadMatcher.get().describeMismatch(payload, description);
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
