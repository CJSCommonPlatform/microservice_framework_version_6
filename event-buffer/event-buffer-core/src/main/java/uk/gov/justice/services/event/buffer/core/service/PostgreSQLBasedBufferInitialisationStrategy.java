package uk.gov.justice.services.event.buffer.core.service;

import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;

import java.util.UUID;

public class PostgreSQLBasedBufferInitialisationStrategy implements BufferInitialisationStrategy {
    private static final long INITIAL_VERSION = 0L;

    private final SubscriptionJdbcRepository subscriptionJdbcRepository;

    public PostgreSQLBasedBufferInitialisationStrategy(final SubscriptionJdbcRepository subscriptionJdbcRepository) {
        this.subscriptionJdbcRepository = subscriptionJdbcRepository;
    }

    @Override
    public long initialiseBuffer(final UUID streamId, final String source) {
        subscriptionJdbcRepository.updateSource(streamId,source);
        subscriptionJdbcRepository.insertOrDoNothing(new Subscription(streamId, INITIAL_VERSION, source));
        return subscriptionJdbcRepository.findByStreamIdAndSource(streamId, source)
                .orElseThrow(() -> new IllegalStateException("stream status cannot be empty"))
                .getPosition();
    }
}
