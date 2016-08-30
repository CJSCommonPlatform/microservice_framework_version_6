package uk.gov.justice.services.core.eventbuffer;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class PassThroughEventBufferServiceTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private PassThroughEventBufferService eventBufferService;

    @Test
    public void shouldReturnJsonEnvelopeInAStream() throws Exception {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Stream<JsonEnvelope> envelopeStream = eventBufferService.currentOrderedEventsWith(jsonEnvelope);
        assertThat(envelopeStream.collect(toList()), contains(jsonEnvelope));
    }
}