package uk.gov.justice.services.adapter.messaging;

import javax.jms.TextMessage;

public interface JmsParameterChecker {

    /**
     * Checks the JMS Interceptor parameters and throws an {@link IllegalArgumentException} if not
     * only one parameter present and the parameter is not a {@link TextMessage}.
     *
     * @param parameters Array of parameter objects
     */
    void check(final Object[] parameters);
}