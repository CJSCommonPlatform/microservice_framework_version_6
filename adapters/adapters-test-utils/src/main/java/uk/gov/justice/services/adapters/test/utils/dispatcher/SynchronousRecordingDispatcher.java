package uk.gov.justice.services.adapters.test.utils.dispatcher;

import uk.gov.justice.services.core.dispatcher.SynchronousDispatcher;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Singleton
public class SynchronousRecordingDispatcher extends BasicRecordingDispatcher implements SynchronousDispatcher {

    private List<MockResponse> mockResponses = new LinkedList<>();

    @Override
    public Envelope dispatch(final Envelope dispatchedEnvelope) {
        record(dispatchedEnvelope);
        return responseTo(dispatchedEnvelope);
    }

    public void setupResponse(String payloadElementNameCriteria, String payloadElementValueCriteria, Envelope envelopeToReturn) {
        mockResponses.add(new MockResponse(payloadElementNameCriteria, payloadElementValueCriteria, envelopeToReturn));
    }

    private Envelope responseTo(Envelope dispatchedEnvelope) {
        Optional<MockResponse> response = mockResponses.stream().filter(r -> r.matches(dispatchedEnvelope)).findFirst();
        return response.isPresent() ? response.get().envelopeToReturn() : null;
    }

    private static class MockResponse {
        private final String payloadElementName;
        private final String payloadElementValue;
        private final Envelope envelopeToReturn;

        MockResponse(String payloadElementName, String payloadElementValue, Envelope envelopeToReturn) {
            this.payloadElementName = payloadElementName;
            this.payloadElementValue = payloadElementValue;
            this.envelopeToReturn = envelopeToReturn;
        }

        boolean matches(Envelope dispatchedEnvelope) {
            return payloadElementValue.equals(dispatchedEnvelope.payload().getString(payloadElementName));
        }

        Envelope envelopeToReturn() {
            return envelopeToReturn;
        }
    }
}
