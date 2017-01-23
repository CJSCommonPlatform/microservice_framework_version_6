package uk.gov.justice.services.generators.test.utils.logger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class TestAppender extends AppenderSkeleton {
    private List<LoggingEvent> messages = new ArrayList<LoggingEvent>();

    @Override
    protected void append(final LoggingEvent loggingEvent) {
        messages.add(loggingEvent);
    }

    @Override
    public void close() {

    }

    public List<LoggingEvent> messages() {
        return messages;
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}