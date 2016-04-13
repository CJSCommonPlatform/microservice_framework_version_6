package uk.gov.justice.services.adapters.test.utils.dispatcher;

import uk.gov.justice.services.core.dispatcher.SynchronousDispatcher;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Singleton
public class SynchronousRecordingDispatcher extends BasicRecordingDispatcher implements SynchronousDispatcher {

    private List<MockResponse> mockResponses = new LinkedList<>();

    @Override
    public JsonEnvelope dispatch(final JsonEnvelope dispatchedJsonEnvelope) {
        record(dispatchedJsonEnvelope);
        return responseTo(dispatchedJsonEnvelope);
    }

    public void setupResponse(String payloadElementNameCriteria, String payloadElementValueCriteria, JsonEnvelope jsonEnvelopeToReturn) {
        mockResponses.add(new MockResponse(payloadElementNameCriteria, payloadElementValueCriteria, jsonEnvelopeToReturn));
    }

    private JsonEnvelope responseTo(JsonEnvelope dispatchedJsonEnvelope) {
        Optional<MockResponse> response = mockResponses.stream().filter(r -> r.matches(dispatchedJsonEnvelope)).findFirst();
        return response.isPresent() ? response.get().envelopeToReturn() : null;
    }

    private static class MockResponse {
        private final String payloadElementName;
        private final String payloadElementValue;
        private final JsonEnvelope jsonEnvelopeToReturn;

        MockResponse(String payloadElementName, String payloadElementValue, JsonEnvelope jsonEnvelopeToReturn) {
            this.payloadElementName = payloadElementName;
            this.payloadElementValue = payloadElementValue;
            this.jsonEnvelopeToReturn = jsonEnvelopeToReturn;
        }

        boolean matches(JsonEnvelope dispatchedJsonEnvelope) {
            return payloadElementValue.equals(dispatchedJsonEnvelope.payloadAsJsonObject().getString(payloadElementName));
        }

        JsonEnvelope envelopeToReturn() {
            return jsonEnvelopeToReturn;
        }
    }
}
