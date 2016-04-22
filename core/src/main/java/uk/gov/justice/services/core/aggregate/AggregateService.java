package uk.gov.justice.services.core.aggregate;

import static java.lang.String.format;

import uk.gov.justice.domain.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Service for replaying event streams on aggregates.
 */
public class AggregateService {

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private ConcurrentHashMap<String, Class> eventMap = new ConcurrentHashMap<>();

    public <T extends Aggregate> T get(final EventStream stream, final Class<T> clazz) {

        try {
            T aggregate = clazz.newInstance();
            stream.read()
                    .map(this::convertEnvelopeToEvent)
                    .forEach(aggregate::apply);
            return aggregate;

        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(format("Could not instantiate aggregate of class %s", clazz.getName()), ex);
        }
    }

    /**
     * Register method, invoked automatically to register all event classes into the eventMap.
     *
     * @param event identified by the framework to be registered into the event map
     */
    void register(@Observes final EventFoundEvent event) {
        eventMap.putIfAbsent(event.getEventName(), event.getClazz());
    }

    private Object convertEnvelopeToEvent(final JsonEnvelope event) {
        final String name = event.metadata().name();
        if (!eventMap.containsKey(name)) {
            throw new IllegalStateException(format("No event class registered for events of type %s", name));
        }

        return jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), eventMap.get(name));
    }
}
