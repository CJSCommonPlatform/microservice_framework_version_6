package uk.gov.justice.services.messaging.logging;

import java.util.function.Supplier;

import org.slf4j.Logger;

public interface TraceLogger {

    void trace(final Logger logger, final Supplier<String> supplier);
}