package uk.gov.justice.raml.jms.converters;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionNamesGeneratorTest {

    @InjectMocks
    private SubscriptionNamesGenerator subscriptionNamesGenerator;

    @Test
    public void shouldGetTheContextNameFromTheBaseUri() throws Exception {

        final String baseUri = "message://event/listener/message/people";

        assertThat(subscriptionNamesGenerator.createContextNameFrom(baseUri), is("people"));
    }

    @Test
    public void shouldCreateASubscriptionNameFromAResourceUri() throws Exception {

        final String resourceUri = "/people.event";

        assertThat(subscriptionNamesGenerator.createSubscriptionNameFrom(resourceUri), is("PeopleEventSubscription"));
    }

    @Test
    public void shouldCreateAnEventSourceNameFromAResourceUri() throws Exception {

        final String resourceUri = "/people.event";

        assertThat(subscriptionNamesGenerator.createEventSourceNameFrom(resourceUri), is("PeopleEventSource"));
    }
}
