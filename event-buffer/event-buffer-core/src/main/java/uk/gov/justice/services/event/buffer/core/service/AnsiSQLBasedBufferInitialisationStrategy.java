package uk.gov.justice.services.event.buffer.core.service;

import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;

import java.util.Optional;
import java.util.UUID;

public class AnsiSQLBasedBufferInitialisationStrategy implements BufferInitialisationStrategy {

    private static final long INITIAL_VERSION = 0L;
    private final SubscriptionJdbcRepository subscriptionJdbcRepository;

    public AnsiSQLBasedBufferInitialisationStrategy(final SubscriptionJdbcRepository subscriptionJdbcRepository) {
        this.subscriptionJdbcRepository = subscriptionJdbcRepository;
    }

    @Override
    public long initialiseBuffer(final UUID streamId, final String source) {
        subscriptionJdbcRepository.updateSource(streamId,source);
        final Optional<Subscription> currentStatus = subscriptionJdbcRepository.findByStreamIdAndSource(streamId, source);

        if (!currentStatus.isPresent()) {
            //this is to address race condition
            //in case of primary key violation the exception gets thrown, event goes back into topic and the transaction gets retried
            subscriptionJdbcRepository.insert(new Subscription(streamId, INITIAL_VERSION, source));
            return INITIAL_VERSION;

        } else {
            return currentStatus.get().getPosition();
        }
    }
}
