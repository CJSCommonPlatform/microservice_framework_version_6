package uk.gov.justice.services.core.jms;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

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
public class DefaultJmsDestinations implements JmsDestinations {

    private final Map<String, String> destinationMap;

    Context initialContext;

    public DefaultJmsDestinations() throws NamingException {
        initialContext = new InitialContext();

        this.destinationMap = new ConcurrentHashMap<>();
        destinationMap.put(COMMAND_CONTROLLER, "%s.controller.command");
        destinationMap.put(COMMAND_HANDLER, "%s.handler.command");
        destinationMap.put(EVENT_LISTENER, "%s.event");
    }

    /**
     * Retrieves the command controller destination based on the <code>component</code> and
     * <code>contextName</code>
     *
     * @param component   Component the endpoint is associated with.
     * @param contextName contextName the endpoint is associated with.
     * @return the destination for the associated service component and context.
     */
    @Override
    public Destination getDestination(final String component, final String contextName) {

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
