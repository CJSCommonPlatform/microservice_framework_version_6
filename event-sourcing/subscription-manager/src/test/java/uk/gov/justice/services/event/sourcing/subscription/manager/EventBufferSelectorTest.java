package uk.gov.justice.services.event.sourcing.subscription.manager;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.event.buffer.api.EventBufferService;

import java.util.Optional;


@RunWith(MockitoJUnitRunner.class)
public class EventBufferSelectorTest {

    @Mock
    private EventBufferService eventBufferService;

    @InjectMocks
    private EventBufferSelector eventBufferSelector;

    @Test
    public void shouldReturnTheEventBufferAsAnOptionalIfTheComponentIsAnEventListener() throws Exception {

        final String componentName = "MY_EVENT_LISTENER";

        final Optional<EventBufferService> eventBufferServiceOptional = eventBufferSelector.selectFor(componentName);

        assertThat(eventBufferServiceOptional.isPresent(), is(true));
        assertThat(eventBufferServiceOptional.get(), is(eventBufferService));
    }

    @Test
    public void shouldReturnTheEventBufferAsEmptyIfTheComponentIsNotAnEventListener() throws Exception {

        final String componentName = "MY_EVENT_PROCESSOR";

        final Optional<EventBufferService> eventBufferServiceOptional = eventBufferSelector.selectFor(componentName);

        assertThat(eventBufferServiceOptional.isPresent(), is(false));
    }
}
