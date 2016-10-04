package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class JsonEnvelopeMatcher extends TypeSafeMatcher<JsonEnvelope> {

    private final JsonEnvelopeMetadataMatcher metadataMatcher;
    private final JsonEnvelopePayloadMatcher payloadMatcher;

    public static JsonEnvelopeMatcher jsonEnvelope(final JsonEnvelopeMetadataMatcher metadataMatcher, final JsonEnvelopePayloadMatcher payloadMatcher) {
        return new JsonEnvelopeMatcher(metadataMatcher, payloadMatcher);
    }

    public JsonEnvelopeMatcher(final JsonEnvelopeMetadataMatcher metadataMatcher, final JsonEnvelopePayloadMatcher payloadMatcher) {
        this.metadataMatcher = metadataMatcher;
        this.payloadMatcher = payloadMatcher;
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
    protected boolean matchesSafely(final JsonEnvelope jsonEnvelope) {
        return metadataMatcher.matches(jsonEnvelope.metadata()) && payloadMatcher.matches(jsonEnvelope.payloadAsJsonObject());
    }
}