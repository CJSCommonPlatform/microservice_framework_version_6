package uk.gov.justice.subscription.yaml.parser;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;

public class YamlParserTest {

    @Test
    public void shouldParseSubscriptionPathAsSubscriptionDescriptorDef() throws Exception {

        final URL url = getFromClasspath("yaml/subscriptions-descriptor.yaml");

        final YamlParser yamlParser = new YamlParser();

        final TypeReference<Map<String, SubscriptionsDescriptor>> typeReference
                = new TypeReference<Map<String, SubscriptionsDescriptor>>() {
        };
        final Map<String, SubscriptionsDescriptor> subscriptionDescriptorDefinitionMap = yamlParser.parseYamlFrom(url, typeReference);
        final SubscriptionsDescriptor subscriptionsDescriptor = subscriptionDescriptorDefinitionMap.get("subscriptions_descriptor");
        assertThat(subscriptionsDescriptor.getService(), is("examplecontext"));
        assertThat(subscriptionsDescriptor.getServiceComponent(), is("EVENT_LISTENER"));
        assertThat(subscriptionsDescriptor.getSpecVersion(), is("1.0.0"));

        final List<Subscription> subscriptions = subscriptionsDescriptor.getSubscriptions();
        assertThat(subscriptions.size(), is(2));

        final Subscription subscription_1 = subscriptions.get(0);
        assertThat(subscription_1.getName(), is("subscription1"));
        assertThat(subscription_1.getEventSourceName(), is("example"));

        final List<Event> events_1 = subscription_1.getEvents();
        assertThat(events_1.size(), is(2));
        assertThat(events_1.get(0).getName(), is("example.recipe-added"));
        assertThat(events_1.get(0).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/example/example.recipe-added.json"));
        assertThat(events_1.get(1).getName(), is("example.recipe-deleted"));
        assertThat(events_1.get(1).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/example/example.recipe-deleted.json"));
    }

    @Test
    public void shouldParseEventSourcesPathAsEventSources() throws Exception {

        final URL url = getFromClasspath("yaml/event-sources.yaml");

        final YamlParser yamlParser = new YamlParser();
        final TypeReference<Map<String, List<EventSourceDefinition>>> typeReference
                = new TypeReference<Map<String, List<EventSourceDefinition>>>() {
        };
        final Map<String, List<EventSourceDefinition>> stringListMap = yamlParser.parseYamlFrom(url, typeReference);
        final List<EventSourceDefinition> eventSourceDefinitions = stringListMap.get("event_sources");
        assertThat(eventSourceDefinitions.size(), is(2));

        final EventSourceDefinition eventSource_Definition_1 = eventSourceDefinitions.get(0);
        assertThat(eventSource_Definition_1.getName(), is("people"));
        assertThat(eventSource_Definition_1.getLocation().getJmsUri(), is("jms:topic:people.event?timeToLive=1000"));
        assertThat(eventSource_Definition_1.getLocation().getRestUri(), is("http://localhost:8080/people/event-source-api/rest"));
        assertThat(eventSource_Definition_1.getLocation().getDataSource(), is(Optional.of("java:/app/peoplewarfilename/DS.eventstore")));

        final EventSourceDefinition eventSource_Definition_2 = eventSourceDefinitions.get(1);
        assertThat(eventSource_Definition_2.getName(), is("example"));
        assertThat(eventSource_Definition_2.getLocation().getJmsUri(), is("jms:topic:example.event?timeToLive=1000"));
        assertThat(eventSource_Definition_2.getLocation().getRestUri(), is("http://localhost:8080/example/event-source-api/rest"));
        assertThat(eventSource_Definition_2.getLocation().getDataSource(), is(Optional.empty()));
    }

    @Test
    public void shouldThrowFileNotFoundExceptionForTypeReference() throws Exception {

        final URL url = get("this-subscription-does-not-exist.yaml").toUri().toURL();
        final TypeReference<SubscriptionsDescriptor> typeReference
                = new TypeReference<SubscriptionsDescriptor>() {
        };

        try {
            final YamlParser yamlParser = new YamlParser();
            yamlParser.parseYamlFrom(url, typeReference);
            fail();
        } catch (final YamlParserException e) {
            assertThat(e.getCause(), is(instanceOf(FileNotFoundException.class)));
            assertThat(e.getMessage(), containsString("Failed to read YAML file"));
            assertThat(e.getMessage(), containsString("this-subscription-does-not-exist.yaml"));
        }
    }

    @Test
    public void shouldThrowFileNotFoundExceptionForClassType() throws Exception {

        final URL url = get("this-subscription-does-not-exist.yaml").toUri().toURL();
        try {
            final YamlParser yamlParser = new YamlParser();
            yamlParser.parseYamlFrom(url, SubscriptionsDescriptor.class);
            fail();
        } catch (final YamlParserException e) {
            assertThat(e.getCause(), is(instanceOf(FileNotFoundException.class)));
            assertThat(e.getMessage(), containsString("Failed to read YAML file"));
            assertThat(e.getMessage(), containsString("this-subscription-does-not-exist.yaml"));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private URL getFromClasspath(final String name) throws MalformedURLException {
        return get(getClass().getClassLoader().getResource(name).getPath()).toUri().toURL();
    }
}
