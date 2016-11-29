package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;

import javax.jms.TextMessage;

/**
 * Checks the parameters received by a JMS Interceptor
 */
public class JmsParametersChecker {

    /**
     * Checks the JMS Interceptor parameters and throws an {@link IllegalArgumentException} if not
     * only one parameter present and the parameter is not a {@link TextMessage}.
     *
     * @param parameters Array of parameter objects
     */
    public void check(final Object[] parameters) {
        if (parameters.length != 1) {
            throw new IllegalArgumentException("Can only be used on single argument methods");
        }

        if (!(parameters[0] instanceof TextMessage)) {
            throw new IllegalArgumentException(
                    format("Can only be used on a JMS TextMessage, not %s", parameters[0].getClass().getName()));
        }
    }
}
