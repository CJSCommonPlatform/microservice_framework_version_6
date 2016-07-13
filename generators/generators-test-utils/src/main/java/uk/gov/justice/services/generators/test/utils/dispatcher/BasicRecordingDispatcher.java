package uk.gov.justice.services.generators.test.utils.dispatcher;

import static com.jayway.awaitility.Awaitility.await;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;


/**
 * Dispatcher for test purposes that records the envelopes it has been asked to dispatch.
 */
public class BasicRecordingDispatcher {
    private final List<JsonEnvelope> recordedEnvelopes = new CopyOnWriteArrayList<>();

    protected void record(final JsonEnvelope envelope) {
        recordedEnvelopes.add(envelope);
    }

    /**
     * Waits for and returns an envelope with json payload containing specified element
     *
     * @param jsonElementName  - name of element in payload
     * @param jsonElementValue - value of element in payload
     * @return - envelope matching given arguments
     */
    public JsonEnvelope awaitForEnvelopeWithPayloadOf(final String jsonElementName, final String jsonElementValue) {
        return awaitForEnvelopeContaining(payloadWith(jsonElementName, jsonElementValue));
    }

    /**
     * Waits for and returns an envelope with json metadata containing specified element
     *
     * @param jsonElementName  - name of element in metadata
     * @param jsonElementValue - value of element in metadata
     * @return - envelope matching given arguments
     */
    public JsonEnvelope awaitForEnvelopeWithMetadataOf(final String jsonElementName, final String jsonElementValue) {
        return awaitForEnvelopeContaining(metadataWith(jsonElementName, jsonElementValue));
    }

    /**
     * Method for testing negative conditions. Checks whether an envelope containing specified
     * element in the payload has (not) been dispatched.
     *
     * @param jsonElementName  - name of element in payload
     * @param jsonElementValue - value of element in payload
     * @return - true if the envelope has not been dispatched, false otherwise
     */
    public boolean notFoundEnvelopeWithPayloadOf(final String jsonElementName, final String jsonElementValue) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return !envelopeMatching(payloadWith(jsonElementName, jsonElementValue)).isPresent();
    }

    /**
     * Method for testing negative conditions. Checks whether an envelope containing specified
     * element in the payload has (not) been dispatched.
     *
     * @param jsonElementName  - name of element in metadata
     * @param jsonElementValue - value of element in metadata
     * @return - true if the envelope has not been dispatched, false otherwise
     */
    public boolean notFoundEnvelopeWithMetadataOf(final String jsonElementName, final String jsonElementValue) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return !envelopeMatching(metadataWith(jsonElementName, jsonElementValue)).isPresent();
    }

    private JsonEnvelope awaitForEnvelopeContaining(final Predicate<JsonEnvelope> filterCondition) {
        await().until(() -> envelopeMatching(filterCondition).isPresent());
        return envelopeMatching(filterCondition).get();
    }

    private Optional<JsonEnvelope> envelopeMatching(final Predicate<JsonEnvelope> filterCondition) {
        return recordedEnvelopes.stream().filter(filterCondition).findFirst();
    }

    private Predicate<JsonEnvelope> payloadWith(final String elementName, final String elementValue) {
        return e -> e.payloadAsJsonObject().getString(elementName) != null && e.payloadAsJsonObject().getString(elementName).equals(elementValue);
    }

    private Predicate<JsonEnvelope> metadataWith(final String elementName, final String elementValue) {
        return e -> e.metadata().asJsonObject().getString(elementName).equals(elementValue);
    }
}
