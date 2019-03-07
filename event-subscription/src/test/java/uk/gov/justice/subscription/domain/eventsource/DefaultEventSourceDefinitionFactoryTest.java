package uk.gov.justice.subscription.domain.eventsource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventSourceDefinitionFactoryTest {

    @InjectMocks
    private DefaultEventSourceDefinitionFactory defaultEventSourceDefinitionFactory;

    @Test
    public void shouldCreateADefaultDataSource() throws Exception {

        defaultEventSourceDefinitionFactory.warFileName = "my-context-service";

        final EventSourceDefinition eventSourceDefinition = defaultEventSourceDefinitionFactory.createDefaultEventSource();

        assertThat(eventSourceDefinition.getName(), is("my-context-service-event-store"));
        assertThat(eventSourceDefinition.isDefault(), is(true));

        final Location location = eventSourceDefinition.getLocation();

        if(location.getDataSource().isPresent()) {
            assertThat(location.getDataSource().get(), is("java:/app/my-context-service/DS.eventstore"));
        } else {
            fail();
        }

        assertThat(location.getJmsUri(), is("JMS URI not used"));
        assertThat(location.getRestUri(), is("Rest URI not used"));
    }
}
