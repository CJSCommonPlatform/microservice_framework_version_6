package uk.gov.justice.subscription.file.read;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import uk.gov.justice.subscription.SubscriptionDescriptorException;
import uk.gov.justice.subscription.domain.Event;
import uk.gov.justice.subscription.domain.Eventsource;
import uk.gov.justice.subscription.domain.Location;
import uk.gov.justice.subscription.domain.Subscription;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorFileValidator;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorParser;
import uk.gov.justice.subscription.file.read.YamlFileToJsonObjectConverter;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import org.everit.json.schema.ValidationException;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionDescriptorParserTest {

    private SubscriptionDescriptorParser subscriptionDescriptorParser;

    @Before
    public void createParser() {
        final YamlFileToJsonObjectConverter yamlFileToJsonObjectConverter = new YamlFileToJsonObjectConverter();
        final SubscriptionDescriptorFileValidator subscriptionDescriptorFileValidator = new SubscriptionDescriptorFileValidator(yamlFileToJsonObjectConverter);
        subscriptionDescriptorParser = new SubscriptionDescriptorParser(subscriptionDescriptorFileValidator);
    }

    @Test
    public void shouldThrowFileNotFoundException() {

        final Path path = get("this-subscription-does-not-exist.yaml").toAbsolutePath();

        try {
            subscriptionDescriptorParser.read(path);
            fail();
        } catch (SubscriptionDescriptorException re) {
            assertThat(re, is(instanceOf(SubscriptionDescriptorException.class)));
            assertThat(re.getCause(), is(instanceOf(NoSuchFileException.class)));
        }
    }

    @Test
    public void shouldFailOnIncorrectSubscriptionYaml() {

        final Path path = getFromClasspath("incorrect-subscription.yaml");

        try {
            subscriptionDescriptorParser.read(path);
            fail();
        } catch (final SubscriptionDescriptorException re) {
            assertThat(re, is(instanceOf(SubscriptionDescriptorException.class)));
            assertThat(re.getCause(), is(instanceOf(ValidationException.class)));
            assertThat(re.getCause().getMessage(), is("#/subscription_descriptor: required key [spec_version] not found"));
        }
    }

    @Test
    public void shouldParsePathsToYaml() {

        final Path path = getFromClasspath("subscription.yaml");

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptorParser.read(path);

            assertSubscriptionDescriptor(subscriptionDescriptor);
            final List<Subscription> subscriptions = subscriptionDescriptor.getSubscriptions();
            for (final Subscription subscription : subscriptions) {
                if (subscription.getName().equalsIgnoreCase("subscription1")) {
                    assertExampleEvents(subscription);
                    assertExampleEventSource(subscription.getEventsource());
                }
                if (subscription.getName().equalsIgnoreCase("subscription2")) {
                    assertPeopleEvents(subscription);
                    assertPeopleEventSource(subscription.getEventsource());
                }
            }
    }

    private void assertExampleEventSource(Eventsource eventsource) {
        final String name = eventsource.getName();
        assertThat(name, is("examplecontext"));
        final Location location = eventsource.getLocation();
        assertThat(location.getJmsUri(), is("jms:topic:example.event"));
        assertThat(location.getRestUri(), is("http://localhost:8080/example/event-source-api/rest"));
    }

    private void assertPeopleEventSource(Eventsource eventsource) {
        final String name = eventsource.getName();
        assertThat(name, is("people"));
        final Location location = eventsource.getLocation();
        assertThat(location.getJmsUri(), is("jms:topic:people.event"));
        assertThat(location.getRestUri(), is("http://localhost:8080/people/event-source-api/rest"));
    }

    private void assertSubscriptionDescriptor(SubscriptionDescriptor subscriptionDescriptor) {
        assertThat(subscriptionDescriptor.getService(), is("examplecontext"));
        assertThat(subscriptionDescriptor.getServiceComponent(), is("EVENT_LISTENER"));
        assertThat(subscriptionDescriptor.getSpecVersion(), is("1.0.0"));
    }

    private void assertExampleEvents(Subscription subscription) {
        final List<Event> events = subscription.getEvents();
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getName(), is("example.recipe-added"));
        assertThat(events.get(0).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/example/example.recipe-added.json"));
        assertThat(events.get(1).getName(), is("example.recipe-deleted"));
        assertThat(events.get(1).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/example/example.recipe-deleted.json"));
    }

    private void assertPeopleEvents(Subscription subscription) {
        final List<Event> events = subscription.getEvents();
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getName(), is("people.person-added"));
        assertThat(events.get(0).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/people/people.person-added.json"));
        assertThat(events.get(1).getName(), is("people.person-removed"));
        assertThat(events.get(1).getSchemaUri(), is("http://justice.gov.uk/json/schemas/domains/people/people.person-removed.json"));
    }

    @SuppressWarnings("ConstantConditions")
    private Path getFromClasspath(final String name) {
        return get(getClass().getClassLoader().getResource(name).getPath());
    }
}
