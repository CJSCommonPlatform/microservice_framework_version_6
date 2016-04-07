package uk.gov.justice.services.adapters.test.utils.dispatcher;

import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Singleton;

/**
 * Dummy dispatcher to be used in component level asynchronous integration testing.
 * Records received envelopes and exposes them through accessor methods that use polling to wait for a specific envelope to arrive.
 */
@Singleton
public class AsynchronousRecordingDispatcher extends BasicRecordingDispatcher implements AsynchronousDispatcher {

    /**
     * Records the envelope
     *
     * @param envelope The {@link Envelope} to recorded.
     */
    @Override
    public void dispatch(Envelope envelope) {
        record(envelope);
    }


}
