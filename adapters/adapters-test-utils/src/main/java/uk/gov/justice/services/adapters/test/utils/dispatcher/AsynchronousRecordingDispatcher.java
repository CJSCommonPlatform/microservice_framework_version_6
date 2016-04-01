package uk.gov.justice.services.adapters.test.utils.dispatcher;

import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static com.jayway.awaitility.Awaitility.await;

/**
 * Dummy dispatcher to be used in component level asynchronous integration testing.
 * Records received envelopes and exposes them through accessor methods that use polling to wait for a specific envelope to arrive.
 */
@Singleton
public class AsynchronousRecordingDispatcher implements AsynchronousDispatcher {
    private final List<Envelope> recordedEnvelopes = new CopyOnWriteArrayList<>();

    /**
     * Records the envelope
     *
     * @param envelope The {@link Envelope} to recorded.
     */
    @Override
    public void dispatch(Envelope envelope) {
        this.recordedEnvelopes.add(envelope);
    }

    /**
     * Waits for and returns an envelope with json payload containing specified element
     *
     * @param jsonElementName  - name of element in payload
     * @param jsonElementValue - value of element in payload
     * @return - envelope matching given arguments
     */
    public Envelope awaitForEnvelopeWithPayloadOf(String jsonElementName, String jsonElementValue) {
        return awaitForEnvelopeContaining(payloadWith(jsonElementName, jsonElementValue));
    }

    /**
     * Waits for and returns an envelope with json metadata containing specified element
     *
     * @param jsonElementName  - name of element in metadata
     * @param jsonElementValue - value of element in metadata
     * @return - envelope matching given arguments
     */
    public Envelope awaitForEnvelopeWithMetadataOf(String jsonElementName, String jsonElementValue) {
        return awaitForEnvelopeContaining(metadataWith(jsonElementName, jsonElementValue));
    }

    /**
     * Method for testing negative conditions. Checks whether an envelope containing specified element in the payload has (not) been dispatched.
     * @param jsonElementName  - name of element in payload
     * @param jsonElementValue - value of element in payload
     * @return - true if the envelope has not been dispatched, false otherwise
     */
    public boolean notFoundEnvelopeWithPayloadOf(String jsonElementName, String jsonElementValue) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return !envelopeMatching(payloadWith(jsonElementName, jsonElementValue)).isPresent();
    }

    /**
     * Method for testing negative conditions. Checks whether an envelope containing specified element in the payload has (not) been dispatched.
     * @param jsonElementName  - name of element in metadata
     * @param jsonElementValue - value of element in metadata
     * @return - true if the envelope has not been dispatched, false otherwise
     */
    public boolean notFoundEnvelopeWithMetadataOf(String jsonElementName, String jsonElementValue) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return !envelopeMatching(metadataWith(jsonElementName, jsonElementValue)).isPresent();
    }

    private Envelope awaitForEnvelopeContaining(Predicate<Envelope> filterCondition) {
        await().until(() -> envelopeMatching(filterCondition).isPresent());
        return envelopeMatching(filterCondition).get();
    }

    private Optional<Envelope> envelopeMatching(Predicate<Envelope> filterCondition) {
        return recordedEnvelopes.stream().filter(filterCondition).findFirst();
    }

    private Predicate<Envelope> payloadWith(String elementName, String elementValue) {
        return e -> e.payload().getString(elementName) != null && e.payload().getString(elementName).equals(elementValue);
    }

    private Predicate<Envelope> metadataWith(String elementName, String elementValue) {
        return e -> e.metadata().asJsonObject().getString(elementName).equals(elementValue);
    }


}
