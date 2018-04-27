package uk.gov.justice.services.event.sourcing.subscription.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.event.sourcing.subscription.QualifierAnnotationExtractor;
import uk.gov.justice.services.event.sourcing.subscription.SubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.SubscriptionManagerProducer;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.subscription.ParserProducer;
import uk.gov.justice.subscription.registry.SubscriptionDescriptorRegistryProducer;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlSchemaLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        ParserProducer.class,
        YamlParser.class,
        YamlSchemaLoader.class,
        ObjectMapperProducer.class,
        SubscriptionManagerProducer.class,
        SubscriptionDescriptorRegistryProducer.class,
        QualifierAnnotationExtractor.class,
        SubscriptionManagerProducerIT.TestEventSourceProducer.class,
        SubscriptionManagerProducerIT.TestClass.class
})
public class SubscriptionManagerProducerIT {

    @Inject
    TestEventSourceProducer testEventSourceProducer;

    @Inject
    TestClass testClass;

    @Test
    public void shouldInjectSubscriptionManagerWithNamedSubscription() throws Exception {

        final SubscriptionManager subscriptionManager = testClass.getSubscriptionManager();

        assertThat(subscriptionManager, is(notNullValue()));
        assertThat(subscriptionManager.getEventSource(), is(testEventSourceProducer.eventSource));
        assertThat(subscriptionManager.getSubscription().getName(), is("private event subscriptions"));
    }

    @ApplicationScoped
    public static class TestClass {

        @Inject
        @SubscriptionName("private event subscriptions")
        SubscriptionManager subscriptionManager;

        public SubscriptionManager getSubscriptionManager() {
            return subscriptionManager;
        }
    }

    public static class TestEventSourceProducer {

        public EventSource eventSource;

        @Inject
        QualifierAnnotationExtractor qualifierAnnotationExtractor;

        @Produces
        @EventSourceName
        public EventSource eventSource(final InjectionPoint injectionPoint) {
            final EventSourceName eventSourceName = qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class);

            if (DEFAULT_EVENT_SOURCE_NAME.equals(eventSourceName.value())) {
                eventSource = mock(EventSource.class);
                return eventSource;
            }

            return null;
        }
    }

}
