package uk.gov.justice.services.messaging.logging;

import java.util.function.Supplier;

import org.slf4j.Logger;

public class DebugLogger {

    public void debug(final Logger logger, final Supplier<String> message) {

        if (logger.isDebugEnabled()) {
            try {
                logger.debug(message.get());
            } catch (final Exception e) {
                logger.error("Could not generate debug log message", e);
            }
        }
    }
}
