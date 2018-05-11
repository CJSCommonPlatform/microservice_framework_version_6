package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventSourceTransformationTest {

    private static final UUID STREAM_ID = randomUUID();

    @Mock
    private EventStreamManager eventStreamManager;

    @InjectMocks
    private DefaultEventSourceTransformation defaultEventSourceTransformation;


    @Test
    public void shouldCloneStream() throws EventStreamException {
        final UUID clonedStreamId = randomUUID();
        when(eventStreamManager.cloneAsAncestor(STREAM_ID)).thenReturn(clonedStreamId);
        final UUID clonedId = defaultEventSourceTransformation.cloneStream(STREAM_ID);

        assertThat(clonedId, is(clonedStreamId));
    }

    @Test
    public void shouldDeleteStream() throws EventStreamException {
        defaultEventSourceTransformation.clearStream(STREAM_ID);

        verify(eventStreamManager).clear(STREAM_ID);
    }
}

