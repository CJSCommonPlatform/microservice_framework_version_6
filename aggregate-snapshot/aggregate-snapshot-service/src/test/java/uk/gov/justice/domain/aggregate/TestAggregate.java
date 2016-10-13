package uk.gov.justice.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.event.EventA;
import uk.gov.justice.domain.event.EventB;
import uk.gov.justice.domain.event.EventC;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.json.JsonObject;

public class TestAggregate implements Aggregate {

    private static final long serialVersionUID = 42L;

    private List<Object> recordedEventStates = new ArrayList<>();

    private int numberOfAppliedEvents = 0;

    public int numberOfAppliedEvents() {
        return numberOfAppliedEvents;
    }

    public List<Object> recordedEvents() {
        return recordedEventStates;
    }

    public Stream<Object> addEvent(JsonEnvelope envelope) {

        JsonObject object = envelope.payloadAsJsonObject();

        String value = object.getString("name");

        return apply(Stream.of(new EventA(value)));
    }

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(EventA.class).apply(x -> {
                    ++numberOfAppliedEvents;
                    recordedEventStates.add(event);
                }),
                when(EventB.class).apply(y -> {
                    ++numberOfAppliedEvents;
                    recordedEventStates.add(event);
                }),
                when(EventC.class).apply(z -> {
                    ++numberOfAppliedEvents;
                    recordedEventStates.add(event);
                }));
    }
}