package uk.gov.justice.services.example.cakeshop.event.listener.replay;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CakeEventStreamMaker {

    private final List<JsonEnvelope> jsonEnvelopes;

    public CakeEventStreamMaker(final List<JsonEnvelope> jsonEnvelopes) {
        this.jsonEnvelopes = jsonEnvelopes;
    }

    public Stream<JsonEnvelope> getAllEventsFrom(final long position, final int pageSize) {

        final List<JsonEnvelope> subList = new ArrayList<>(pageSize);

        for (int i = (int) position; i < position + pageSize; i++) {
            if (i == jsonEnvelopes.size()) {
                break;
            }

            subList.add(jsonEnvelopes.get(i));
        }

        return subList.stream();
    }
}

