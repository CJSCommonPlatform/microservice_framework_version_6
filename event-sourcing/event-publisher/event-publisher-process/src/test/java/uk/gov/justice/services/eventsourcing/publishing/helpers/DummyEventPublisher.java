package uk.gov.justice.services.eventsourcing.publishing.helpers;

import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DummyEventPublisher implements EventPublisher {

    private final List<JsonEnvelope> jsonEnvelopes = new ArrayList<>();

    @Override
    public void publish(final JsonEnvelope envelope) {
        jsonEnvelopes.add(envelope);
    }

    public List<JsonEnvelope> getJsonEnvelopes() {
        return jsonEnvelopes;
    }
}
