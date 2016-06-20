package uk.gov.justice.test.util;

import static java.util.Arrays.asList;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Destination;

@ApplicationScoped
public class RecordingJmsEnvelopeSender implements JmsEnvelopeSender {

    private Map<String, List<JsonEnvelope>> recordedEnvelopesSentToDestinations = new ConcurrentHashMap<>();

    public void init() {
        recordedEnvelopesSentToDestinations.clear();
    }


    @Override
    public void send(final JsonEnvelope envelope, final Destination destination) {

    }

    @Override
    public void send(final JsonEnvelope envelope, final String destinationName) {
        record(envelope, destinationName);
    }

    public List<JsonEnvelope> envelopesSentTo(final String destinationName) {
        return recordedEnvelopesSentToDestinations.get(destinationName);
    }



    private void record(final JsonEnvelope envelope, final String destinationName) {
        final List<JsonEnvelope> recordedEnvelopes =
                recordedEnvelopesSentToDestinations.putIfAbsent(destinationName, new LinkedList<>(asList(envelope)));
        if (recordedEnvelopes!=null) {
            recordedEnvelopes.add(envelope);
        }
    }
}
