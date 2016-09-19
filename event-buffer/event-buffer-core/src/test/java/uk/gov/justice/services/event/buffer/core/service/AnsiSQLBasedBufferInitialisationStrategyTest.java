package uk.gov.justice.services.event.buffer.core.service;

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

        final UUID streamId = UUID.randomUUID();
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.empty());
        bufferInitialisationStrategy.initialiseBuffer(streamId);

        verify(streamStatusRepository).insert(new StreamStatus(streamId, 0L));
    }

    @Test
    public void shouldReturnVersionZeroIfBufferStatusDoesNotExist() throws Exception {

        final UUID streamId = UUID.randomUUID();
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.empty());
        assertThat(bufferInitialisationStrategy.initialiseBuffer(streamId), is(0L));

    }

    @Test
    public void shouldNotAddStatusIfItExists() throws Exception {
        final UUID streamId = UUID.randomUUID();
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, 3L)));
        bufferInitialisationStrategy.initialiseBuffer(streamId);

        verify(streamStatusRepository).findByStreamId(streamId);
        verifyNoMoreInteractions(streamStatusRepository);
    }

    @Test
    public void shouldReturnCurrentVersionIfItExists() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final long currentVersion = 3L;
        when(streamStatusRepository.findByStreamId(streamId)).thenReturn(Optional.of(new StreamStatus(streamId, currentVersion)));
        bufferInitialisationStrategy.initialiseBuffer(streamId);

        assertThat(bufferInitialisationStrategy.initialiseBuffer(streamId), is(currentVersion));
    }
}