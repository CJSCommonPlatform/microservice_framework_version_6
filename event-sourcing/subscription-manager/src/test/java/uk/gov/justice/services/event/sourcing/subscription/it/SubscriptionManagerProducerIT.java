package uk.gov.justice.services.event.sourcing.subscription.it;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.fieldValue;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.event.sourcing.subscription.DefaultSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.SubscriptionManagerProducer;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.subscription.ParserProducer;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionDescriptorDefinitionRegistryProducer;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        ParserProducer.class,
        YamlParser.class,
        YamlSchemaLoader.class,
        ObjectMapperProducer.class,
        SubscriptionManagerProducer.class,
        SubscriptionDescriptorDefinitionRegistryProducer.class,
        LoggerProducer.class,
        DispatcherFactory.class,
        JsonEnvelopeRepacker.class,
        EnvelopePayloadTypeConverter.class,
        SubscriptionManagerProducerIT.TestEventSourceProducer.class,
        SubscriptionManagerProducerIT.TestClass.class
})
public class SubscriptionManagerProducerIT {

    private static final String SUBSCRIPTION_NAME = "private event subscriptions";
    private static final String EVENT_SOURCE_NAME = "private.event.source";

    @Inject
    TestEventSourceProducer testEventSourceProducer;

    @Inject
    TestClass testClass;

    @Test
    @Ignore
    public void shouldInjectSubscriptionManagerWithNamedSubscription() throws Exception {

        final SubscriptionManager subscriptionManager = testClass.getSubscriptionManager();

        assertThat(subscriptionManager, is(instanceOf(DefaultSubscriptionManager.class)));

        final DefaultSubscriptionManager defaultSubscriptionManager = (DefaultSubscriptionManager) subscriptionManager;

        final Optional<Object> actualEventSource = fieldValue(defaultSubscriptionManager, "eventSource");
        assertThat(actualEventSource, is(Optional.ofNullable(testEventSourceProducer.getEventSource())));

        final Optional<Object> subscription = fieldValue(defaultSubscriptionManager, "subscription");
        assertThat(subscription.isPresent(), is(true));
        assertThat(((Subscription) subscription.get()).getName(), is(SUBSCRIPTION_NAME));
        assertThat(((Subscription) subscription.get()).getEventSourceName(), is(EVENT_SOURCE_NAME));
    }

    @ApplicationScoped
    @ServiceComponent(EVENT_LISTENER)
    public static class TestClass {

        @Inject
        @SubscriptionName(SUBSCRIPTION_NAME)
        SubscriptionManager subscriptionManager;

        public SubscriptionManager getSubscriptionManager() {
            return subscriptionManager;
        }
    }

    @ApplicationScoped
    public static class TestEventSourceProducer {

        private EventSource eventSource;

        @Inject
        QualifierAnnotationExtractor qualifierAnnotationExtractor;

        @Produces
        @EventSourceName
        public EventSource eventSource(final InjectionPoint injectionPoint) {
            final EventSourceName eventSourceName = qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class);

            if (eventSourceName.value().equals(EVENT_SOURCE_NAME)) {
                eventSource = mock(EventSource.class);
                return eventSource;
            }

            return null;
        }

        public EventSource getEventSource() {
            return eventSource;
        }
    }
}
