package uk.gov.justice.services.eventsourcing.repository.jdbc;


import uk.gov.justice.services.common.configuration.GlobalValue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class EventLogInsertionStrategyProducer {

    private static final String INSTANTIATION_ERROR_MSG = "Could not instantiate event log insertion strategy.";

    @Inject
    @GlobalValue(key = "eventlog.insertion.strategy", defaultValue = "uk.gov.justice.services.eventsourcing.repository.jdbc.PostgresSQLEventLogInsertionStrategy")
    String strategyClass;

    @Inject
    Logger logger;

    @Produces
    public EventLogInsertionStrategy eventLogInsertionStrategy() {
        logger.info("Instantiating {}", strategyClass);
        try {
            final Class<?> clazz = Class.forName(strategyClass);
            return (EventLogInsertionStrategy) clazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(INSTANTIATION_ERROR_MSG, e);
        }
    }
}
