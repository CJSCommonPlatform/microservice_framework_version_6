package uk.gov.justice.services.event.buffer.core.service;


import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class BufferInitialisationStrategyProducer {

    private static final String INSTANTIATION_ERROR_MSG = "Could not instantiate buffer initialisation strategy.";

    @Inject
    @GlobalValue(key = "event.buffer.init.strategy", defaultValue = "uk.gov.justice.services.event.buffer.core.service.PostgreSQLBasedBufferInitialisationStrategy")
    String strategyClass;

    @Inject
    Logger logger;

    @Inject
    SubscriptionJdbcRepository subscriptionJdbcRepository;

    @Produces
    public BufferInitialisationStrategy bufferInitialisationStrategy() {
        logger.info("Instantiating {}", strategyClass);
        try {
            Class<?> clazz = Class.forName(strategyClass);
            Constructor<?> constructor = clazz.getConstructor(SubscriptionJdbcRepository.class);
            return (BufferInitialisationStrategy) constructor.newInstance(subscriptionJdbcRepository);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException(INSTANTIATION_ERROR_MSG, e);
        }
    }
}
