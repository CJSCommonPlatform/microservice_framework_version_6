package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates endpoints based on the context.
 */

@ApplicationScoped
public class JmsEndpoints {

    private final Map<Component, String> endpointMap;

    public JmsEndpoints() {
        this.endpointMap = new ConcurrentHashMap<>();
        endpointMap.put(Component.COMMAND_CONTROLLER, "%s.controller.commands");
        endpointMap.put(Component.COMMAND_HANDLER, "%s.handler.commands");
    }

    /**
     * Retrieves the command controller endpoint based on the <code>component</code> and <code>contextName</code>
     *
     * @param component   Component the endpoint is associated with.
     * @param contextName contextName the endpoint is associated with.
     * @return the endpoint string for the associated service component and context.
     */
    public String getEndpoint(final Component component, final String contextName) {

        if (!endpointMap.containsKey(component)) {
            throw new IllegalArgumentException("No endpoint defined for component of type " + component);
        }

        return String.format(endpointMap.get(component), contextName);
    }

}
