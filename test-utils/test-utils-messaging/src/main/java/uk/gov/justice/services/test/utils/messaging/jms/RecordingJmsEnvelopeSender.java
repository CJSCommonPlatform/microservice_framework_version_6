package uk.gov.justice.services.test.utils.messaging.jms;

import static java.util.Collections.emptyList;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecordingJmsEnvelopeSender implements JmsEnvelopeSender {

    private Map<String, List<JsonEnvelope>> recordedEnvelopesSentToDestinations = new ConcurrentHashMap<>();

    public void clear() {
        recordedEnvelopesSentToDestinations.clear();
    }

    @Override
    public void send(final JsonEnvelope envelope, final String destinationName) {
        recordedEnvelopesSentToDestinations
                .computeIfAbsent(destinationName, key -> new ArrayList<>())
                .add(envelope);
    }

    public List<JsonEnvelope> envelopesSentTo(final String destinationName) {

        if (recordedEnvelopesSentToDestinations.containsKey(destinationName)) {
            return recordedEnvelopesSentToDestinations.get(destinationName);
        }

        return emptyList();
    }
}
