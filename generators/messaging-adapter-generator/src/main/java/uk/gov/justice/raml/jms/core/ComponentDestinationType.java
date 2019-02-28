package uk.gov.justice.raml.jms.core;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Component;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * Maps a {@link Component} to the input {@link Destination} type.
 */
public class ComponentDestinationType {

    private final Map<String, Class<? extends Destination>> components = new HashMap<>();

    public ComponentDestinationType() {
        components.put(COMMAND_API, Queue.class);
        components.put(COMMAND_CONTROLLER, Queue.class);
        components.put(COMMAND_HANDLER, Queue.class);
        components.put(EVENT_PROCESSOR, Topic.class);
        components.put(EVENT_LISTENER, Topic.class);
        components.put(EVENT_INDEXER, Topic.class);
    }

    /**
     * Returns the input {@link Destination} type for the given {@link Component} or throws {@link
     * IllegalArgumentException} if no type found.
     *
     * @param component the component
     * @return the {@link Destination} type or null if no type
     * @throws IllegalArgumentException if no input {@link Destination} found for the given {@link
     *                                  Component}
     */
    public Class<? extends Destination> inputTypeFor(final String component) {
        if (component.contains(COMMAND_API)) {
            return components.get(COMMAND_API);
        }

        if (component.contains(COMMAND_CONTROLLER)) {
            return components.get(COMMAND_CONTROLLER);
        }

        if (component.contains(COMMAND_HANDLER)) {
            return components.get(COMMAND_HANDLER);
        }

        if (component.contains(EVENT_PROCESSOR)) {
            return components.get(EVENT_PROCESSOR);
        }

        if (component.contains(EVENT_LISTENER)) {
            return components.get(EVENT_LISTENER);
        }

        if (component.contains(EVENT_INDEXER)) {
            return components.get(EVENT_INDEXER);
        }

        if (components.containsKey(component)) {
            return components.get(component);
        }

        throw new IllegalArgumentException("No input destination type defined for service component of type " + component);
    }

    public boolean isSupported(final String component) {
        return component.contains(COMMAND_API)
                || component.contains(COMMAND_CONTROLLER)
                || component.contains(COMMAND_HANDLER)
                || component.contains(EVENT_PROCESSOR)
                || component.contains(EVENT_LISTENER)
                || component.contains(EVENT_INDEXER)
                || components.containsKey(component);
    }
}