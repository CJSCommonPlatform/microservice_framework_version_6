package uk.gov.justice.services.event.buffer.core.service;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLBasedBufferInitialisationStrategyTest {

    @Mock
    private StreamStatusJdbcRepository streamStatusRepository;

    private BufferInitialisationStrategy bufferInitialisationStrategy;

    @Before
    public void setUp() throws Exception {
        bufferInitialisationStrategy = new PostgreSQLBasedBufferInitialisationStrategy(streamStatusRepository);
    }

    @Test
    public void shouldTryInsertingZeroBufferStatus() throws Exception {
        final UUID streamId = randomUUID();
        final String source = "a source";
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(new StreamStatus(streamId, 3, source)));
        bufferInitialisationStrategy.initialiseBuffer(streamId, source);

        verify(streamStatusRepository).insertOrDoNothing(new StreamStatus(streamId, 0, source));
    }

    @Test
    public void shouldReturnCurrentVersion() {
        final UUID streamId = randomUUID();
        final String source = "a source";
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(new StreamStatus(streamId, 3, source)));

        final long currentVersion = bufferInitialisationStrategy.initialiseBuffer(streamId, source);
        assertThat(currentVersion, is(3L));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfStatusNotFound() throws Exception {
        final UUID streamId = randomUUID();
        final String source = "a source";
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(empty());

        bufferInitialisationStrategy.initialiseBuffer(streamId, source);

    }
}
