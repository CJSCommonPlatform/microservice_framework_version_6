package uk.gov.justice.services.messaging.subscription.cms;

import static java.nio.file.Paths.get;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import uk.gov.justice.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptorDef;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDescriptorDefLoaderTest {

    @InjectMocks
    private SubscriptionDescriptorDefLoader subscriptionDescriptorDefLoader;

    @Test
    public void shouldLoadARealSubscriptionFileFromTheClasspath() throws Exception {

        final Path subscriptionDefPath = get("subscriptions/simple-subscription-def.yaml");

        final URL subscriptionDef = getClass()
                .getClassLoader()
                .getResource(subscriptionDefPath.toString());

        final Path path = Paths.get(subscriptionDef.toURI());

        final SubscriptionDescriptorDef subscriptionDescriptorDef = subscriptionDescriptorDefLoader
                .loadFrom(path);

        assertThat(subscriptionDescriptorDef, is(notNullValue()));

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptorDef.getSubscriptionDescriptor();
        assertThat(subscriptionDescriptor.getService(), is("exampleContext"));
        assertThat(subscriptionDescriptor.getServiceComponent(), is("EVENT_LISTENER"));
        assertThat(subscriptionDescriptor.getSpecVersion(), is("1.0.0"));

        final List<Subscription> subscriptions = subscriptionDescriptor.getSubscriptions();

        assertThat(subscriptions.get(0).getName(), is("subscription_1"));

        assertThat(subscriptions.get(0).getEvents().size(), is(1));

        assertThat(subscriptions.get(0).getEvents().get(0).getName(), is("event_1"));
        assertThat(subscriptions.get(0).getEvents().get(0).getSchemaUri(), is("schema_1.json"));
        assertThat(subscriptions.get(0).getEventsource().getName(), is("exampleContextEventSource"));

        assertThat(subscriptions.get(0).getEventsource().getLocation().getRestUri(), is("http://localhost:8080/example/event-source-api/rest"));
        assertThat(subscriptions.get(0).getEventsource().getLocation().getJmsUri(), is("jms:topic:example.event"));
    }
}
