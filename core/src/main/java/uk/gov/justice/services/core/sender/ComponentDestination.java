package uk.gov.justice.services.core.sender;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

/**
 * Contains the relationship between the various service components in the same pillar.
 */
@ApplicationScoped
public class ComponentDestination {

    private final Map<Component, Component> destinationComponentMap;

    public ComponentDestination() {
        destinationComponentMap = new ConcurrentHashMap<>();
        destinationComponentMap.put(COMMAND_API, COMMAND_CONTROLLER);
        destinationComponentMap.put(COMMAND_CONTROLLER, COMMAND_HANDLER);
    }

    /**
     * Get the default destination component for <code>component</code>.
     *
     * @param component the component whose destination {@link Component} is required.
     * @return the default destination {@link Component} for <code>component</code>.
     */
    public Component getDefault(final Component component) {
        if (!destinationComponentMap.containsKey(component)) {
            throw new IllegalArgumentException("No default destination defined for service component of type " + component);
        }

        return destinationComponentMap.get(component);
    }

}
