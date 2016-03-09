package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.exception.JmsSenderException;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates endpoints based on the context.
 */

@ApplicationScoped
public class JmsEndpoints {

    private final Map<Component, String> endpointMap;

    Context initialContext;

    public JmsEndpoints() throws NamingException {
        initialContext = new InitialContext();

        this.endpointMap = new ConcurrentHashMap<>();
        endpointMap.put(Component.COMMAND_CONTROLLER, "%s.controller.commands");
        endpointMap.put(Component.COMMAND_HANDLER, "%s.handler.commands");
        endpointMap.put(Component.EVENT_LISTENER, "%s.events");
    }

    /**
     * Retrieves the command controller endpoint based on the <code>component</code> and <code>contextName</code>
     *
     * @param component   Component the endpoint is associated with.
     * @param contextName contextName the endpoint is associated with.
     * @return the endpoint string for the associated service component and context.
     */
    public Destination getEndpoint(final Component component, final String contextName) {

        String jndiName = null;

        try {
            if (!endpointMap.containsKey(component)) {
                throw new IllegalArgumentException("No endpoint defined for component of type " + component);
            }
            jndiName = String.format(endpointMap.get(component), contextName);
            return (Destination) initialContext.lookup(jndiName);
        } catch (NamingException e) {
            throw new JmsSenderException(String.format("Error while looking up JMS destination %s", jndiName), e);
        }
    }

}
