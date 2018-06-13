package uk.gov.justice.services.event.buffer.core.service;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
public class AnsiSQLBasedBufferInitialisationStrategyTest {

    @Mock
    private StreamStatusJdbcRepository streamStatusRepository;


    private BufferInitialisationStrategy bufferInitialisationStrategy;

    @Before
    public void setUp() throws Exception {
        bufferInitialisationStrategy = new AnsiSQLBasedBufferInitialisationStrategy(streamStatusRepository);
    }

    @Test
    public void shouldAddZeroStatusIfItDoesNotExist() throws Exception {

        final UUID streamId = randomUUID();
        final String source = "a source";
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(Optional.empty());
        bufferInitialisationStrategy.initialiseBuffer(streamId, source);

        verify(streamStatusRepository).insert(new StreamStatus(streamId, 0L, source));
    }

    @Test
    public void shouldReturnVersionZeroIfBufferStatusDoesNotExist() throws Exception {

        final UUID streamId = randomUUID();
        final String source = "a source";
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(Optional.empty());
        assertThat(bufferInitialisationStrategy.initialiseBuffer(streamId, source), is(0L));
    }

    @Test
    public void shouldNotAddStatusIfItExists() throws Exception {
        final UUID streamId = randomUUID();
        final String source = "a source";
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(new StreamStatus(streamId, 3L, source)));
        bufferInitialisationStrategy.initialiseBuffer(streamId, source);

        verify(streamStatusRepository).findByStreamIdAndSource(streamId, source);
        verify(streamStatusRepository).updateSource(streamId, source);

        verifyNoMoreInteractions(streamStatusRepository);
    }

    @Test
    public void shouldReturnCurrentVersionIfItExists() throws Exception {
        final UUID streamId = randomUUID();
        final String source = "a source";
        final long currentVersion = 3L;
        when(streamStatusRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(new StreamStatus(streamId, currentVersion, source)));
        bufferInitialisationStrategy.initialiseBuffer(streamId, source);

        assertThat(bufferInitialisationStrategy.initialiseBuffer(streamId, source), is(currentVersion));
    }
}
