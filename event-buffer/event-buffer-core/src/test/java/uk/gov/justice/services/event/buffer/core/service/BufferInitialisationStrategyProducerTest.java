package uk.gov.justice.services.event.buffer.core.service;


import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

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
    private StreamStatusJdbcRepository streamStatusRepository;

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
        final UUID streamId = UUID.randomUUID();
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 0L)));
        bufferInitialisationStrategy.initialiseBuffer(streamId);
        verify(streamStatusRepository).findByStreamId(streamId);
    }

    @Test
    public void shouldPassRepositoryToAnsiStrategy() throws Exception {
        strategyProducer.strategyClass = "uk.gov.justice.services.event.buffer.core.service.AnsiSQLBasedBufferInitialisationStrategy";
        final BufferInitialisationStrategy bufferInitialisationStrategy = strategyProducer.bufferInitialisationStrategy();
        final UUID streamId = UUID.randomUUID();
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 0L)));
        bufferInitialisationStrategy.initialiseBuffer(streamId);
        verify(streamStatusRepository).findByStreamId(streamId);
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