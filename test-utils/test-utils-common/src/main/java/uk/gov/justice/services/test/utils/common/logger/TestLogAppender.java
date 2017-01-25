package uk.gov.justice.services.test.utils.common.logger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class TestLogAppender extends AppenderSkeleton {
    private List<LoggingEvent> logEntries = new ArrayList<LoggingEvent>();
    private final Logger logger;

    private TestLogAppender(final Logger logger) {
        this.logger = logger;
    }

    public static TestLogAppender activate() {
        final Logger logger = Logger.getRootLogger();
        final TestLogAppender appender = new TestLogAppender(logger);
        logger.addAppender(appender);
        return appender;
    }

    public void deactivate() {
        logger.removeAppender(this);
    }

    @Override
    protected void append(final LoggingEvent loggingEvent) {
        logEntries.add(loggingEvent);
    }

    @Override
    public void close() {

    }

    public List<LoggingEvent> logEntries() {
        return logEntries;
    }

    public LoggingEvent firstLogEntry() {
        if (logEntries.size()>0) {
            return logEntries.get(0);
        } else {
            throw new AssertionError("No log entries");
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}