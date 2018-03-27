package uk.gov.justice.services.messaging.subscription.cms;

import static java.nio.file.Paths.get;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import uk.gov.justice.domain.subscriptiondescriptor.Event;
import uk.gov.justice.domain.subscriptiondescriptor.Eventsource;
import uk.gov.justice.domain.subscriptiondescriptor.Location;
import uk.gov.justice.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.maven.generator.io.files.parser.SubscriptionDescriptorFileValidator;
import uk.gov.justice.maven.generator.io.files.parser.YamlFileToJsonObjectConverter;

import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SubscriptionSpecProviderIT {

    private SubscriptionSpecProvider subscriptionSpecProvider;

    @Before
    public void createTheSubscriptionSpecProvider() {

        subscriptionSpecProvider = new SubscriptionSpecProvider();

        subscriptionSpecProvider.classpathPathToAbsolutePathConverter = new ClasspathPathToAbsolutePathConverter();
        subscriptionSpecProvider.subscriptionDescriptorDefLoader = new SubscriptionDescriptorDefLoader();
        subscriptionSpecProvider.subscriptionDescriptorFileValidator = new SubscriptionDescriptorFileValidator(
                new YamlFileToJsonObjectConverter());
    }

    @Test
    public void shouldLoadARealSubscriptionFileFromTheClasspath() throws Exception {

        final Path subscriptionDefPath = get("subscriptions/my-context-event-listener-subscription-def.yaml");

        final SubscriptionDescriptorDef subscriptionDescriptorDef = subscriptionSpecProvider
                .loadFromClasspath(subscriptionDefPath);

        assertThat(subscriptionDescriptorDef, is(notNullValue()));

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptorDef.getSubscriptionDescriptor();
        assertThat(subscriptionDescriptor.getService(), is("exampleContext"));
        assertThat(subscriptionDescriptor.getServiceComponent(), is("EVENT_LISTENER"));
        assertThat(subscriptionDescriptor.getSpecVersion(), is("1.0.0"));

        final List<Subscription> subscriptions = subscriptionDescriptor.getSubscriptions();

        assertThat(subscriptions.size(), is(2));

        assertThat(subscriptions.get(0).getName(), is("subscription_1"));

        final List<Event> events_1 = subscriptions.get(0).getEvents();
        assertThat(events_1.size(), is(2));

        assertThat(events_1.get(0).getName(), is("example.recipe-added"));
        assertThat(events_1.get(0).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/example/example.recipe-added.json"));
        assertThat(events_1.get(1).getName(), is("example.recipe-deleted"));
        assertThat(events_1.get(1).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/example/example.recipe-deleted.json"));

        final Eventsource eventsource_1 = subscriptions.get(0).getEventsource();
        assertThat(eventsource_1.getName(), is("exampleContextEventSource"));

        final Location location_1 = eventsource_1.getLocation();
        assertThat(location_1.getRestUri(), is("http://localhost:8080/example/event-source-api/rest"));
        assertThat(location_1.getJmsUri(), is("jms:topic:example.event"));


        assertThat(subscriptions.get(1).getName(), is("subscription_2"));

        final List<Event> events_2 = subscriptions.get(1).getEvents();
        assertThat(events_2.size(), is(2));

        assertThat(events_2.get(0).getName(), is("people.person-added"));
        assertThat(events_2.get(0).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/people/people.person-added.json"));
        assertThat(events_2.get(1).getName(), is("people.person-removed"));
        assertThat(events_2.get(1).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/people/people.person-removed.json"));

        final Eventsource eventsource_2 = subscriptions.get(1).getEventsource();
        assertThat(eventsource_2.getName(), is("people"));

        final Location location_2 = eventsource_2.getLocation();
        assertThat(location_2.getRestUri(), is("http://localhost:8080/people/event-source-api/rest"));
        assertThat(location_2.getJmsUri(), is("jms:topic:people.event"));

    }
}
