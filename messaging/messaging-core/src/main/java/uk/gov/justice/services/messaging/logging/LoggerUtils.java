package uk.gov.justice.services.messaging.logging;

import org.slf4j.Logger;

import java.util.function.Supplier;
;

public final class LoggerUtils {

    private LoggerUtils(){}

    public static void trace(final Logger logger,
                           final Supplier<String> supplier) {

        if(logger.isTraceEnabled()) {
            try {
                logger.trace(supplier.get());
            } catch (Exception e) {
                logger.error("Could not generate trace log message", e);
            }
        }
    }

}
