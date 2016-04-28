package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.exception.JmsSenderException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Generates endpoints based on the context.
 */

@ApplicationScoped
public class JmsDestinations {

    private final Map<Component, String> destinationMap;

    Context initialContext;

    public JmsDestinations() throws NamingException {
        initialContext = new InitialContext();

        this.destinationMap = new ConcurrentHashMap<>();
        destinationMap.put(Component.COMMAND_CONTROLLER, "%s.controller.command");
        destinationMap.put(Component.COMMAND_HANDLER, "%s.handler.command");
        destinationMap.put(Component.EVENT_LISTENER, "%s.event");
    }

    /**
     * Retrieves the command controller destination based on the <code>component</code> and
     * <code>contextName</code>
     *
     * @param component   Component the endpoint is associated with.
     * @param contextName contextName the endpoint is associated with.
     * @return the destination for the associated service component and context.
     */
    public Destination getDestination(final Component component, final String contextName) {

        String jndiName = null;

        try {
            if (!destinationMap.containsKey(component)) {
                throw new IllegalArgumentException("No endpoint defined for component of type " + component);
            }
            jndiName = String.format(destinationMap.get(component), contextName);
            return (Destination) initialContext.lookup(jndiName);
        } catch (NamingException e) {
            throw new JmsSenderException(String.format("Error while looking up JMS destination %s", jndiName), e);
        }
    }

}
