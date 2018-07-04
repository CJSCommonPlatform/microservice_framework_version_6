package uk.gov.justice.services.event.buffer.core.service;


import static java.util.UUID.randomUUID;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;

import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class BufferInitialisationStrategyProducerTest {

    @Mock
    private Logger logger;

    @Mock
    private SubscriptionJdbcRepository subscriptionJdbcRepository;

    @InjectMocks
    private BufferInitialisationStrategyProducer strategyProducer;


    @Test
    public void shouldProducePostgresStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.event.buffer.core.service.PostgreSQLBasedBufferInitialisationStrategy";
        assertThat(strategyProducer.bufferInitialisationStrategy(), instanceOf(PostgreSQLBasedBufferInitialisationStrategy.class));
    }

    @Test
    public void shouldProduceAnsiSQLStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.event.buffer.core.service.AnsiSQLBasedBufferInitialisationStrategy";
        assertThat(strategyProducer.bufferInitialisationStrategy(), instanceOf(AnsiSQLBasedBufferInitialisationStrategy.class));
    }

    @Test
    public void shouldPassRepositoryToPostgresStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.event.buffer.core.service.PostgreSQLBasedBufferInitialisationStrategy";
        final BufferInitialisationStrategy bufferInitialisationStrategy = strategyProducer.bufferInitialisationStrategy();
        final UUID streamId = randomUUID();
        final String source = "a source";
        when(subscriptionJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(Optional.of(new Subscription(streamId, 0L, source)));
        bufferInitialisationStrategy.initialiseBuffer(streamId, source);
        verify(subscriptionJdbcRepository).findByStreamIdAndSource(streamId, source);
    }

    @Test
    public void shouldPassRepositoryToAnsiStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.event.buffer.core.service.AnsiSQLBasedBufferInitialisationStrategy";
        final BufferInitialisationStrategy bufferInitialisationStrategy = strategyProducer.bufferInitialisationStrategy();
        final UUID streamId = randomUUID();
        final String source = "a source";
        when(subscriptionJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(Optional.of(new Subscription(streamId, 0L, source)));
        bufferInitialisationStrategy.initialiseBuffer(streamId, source);
        verify(subscriptionJdbcRepository).findByStreamIdAndSource(streamId, source);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfClassDoesNotExist() {
        strategyProducer.strategyClass = "uk.gov.justice.services.event.buffer.core.service.SomeUnknowClazzz";

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Could not instantiate buffer initialisation strategy");

        strategyProducer.bufferInitialisationStrategy();

    }

}
