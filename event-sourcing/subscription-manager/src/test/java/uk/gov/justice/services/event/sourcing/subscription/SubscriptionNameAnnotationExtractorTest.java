package uk.gov.justice.services.event.sourcing.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.SubscriptionName;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.InjectionPoint;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionNameAnnotationExtractorTest {

    @InjectMocks
    private SubscriptionNameAnnotationExtractor subscriptionNameAnnotationExtractor;

    @Test
    public void shouldGetTheSubscriptionNameQualifierFromAnEventListenerClass() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final Annotation annotation = MyEventListener.class.getDeclaredAnnotation(SubscriptionName.class);

        when(injectionPoint.getQualifiers()).thenReturn(ImmutableSet.of(annotation));

        final SubscriptionName subscriptionNameAnnotation = subscriptionNameAnnotationExtractor.getFrom(injectionPoint);

        assertThat(subscriptionNameAnnotation.value(), is("my-subscription"));
    }

    @Test
    public void shouldThrowARuntimeExceptionIfNoSubscriptionNameQualifierOnAnEventListener() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class, RETURNS_DEEP_STUBS.get());

        final Set<Annotation> emptySet = new HashSet<>();

        when(injectionPoint.getQualifiers()).thenReturn(emptySet);
        when(injectionPoint.getBean().getName()).thenReturn(MyEventListener.class.getName());

        try {
            subscriptionNameAnnotationExtractor.getFrom(injectionPoint);
            fail();
        } catch (final SubscriptionManagerProducerException expected) {
            assertThat(expected.getMessage(), is("Failed to find SubscriptionName annotation on EventListener 'uk.gov.justice.services.event.sourcing.subscription.MyEventListener'"));
        }
    }
}

@ServiceComponent(EVENT_LISTENER)
@SubscriptionName("my-subscription")
class MyEventListener {

    @Handles("my-context.something-happened")
    public void somethingHappened(final JsonEnvelope event) {

    }
}

@ServiceComponent(EVENT_LISTENER)
class MyOtherEventListener {

    @Handles("my-context.something-else-happened")
    public void somethingHappened(final JsonEnvelope event) {

    }
}
