package uk.gov.justice.services.eventsource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventDestinationResolverTest {

    private static final String NAME = "test.event.listener";
    private DefaultEventDestinationResolver destinationResolver = new DefaultEventDestinationResolver();

    @Test
    public void shouldReturnDestinationName() {
        assertThat(destinationResolver.destinationNameOf("context1.command.abc"), is("context1.event"));
        assertThat(destinationResolver.destinationNameOf("test.command.bcde"), is("test.event"));
    }

}