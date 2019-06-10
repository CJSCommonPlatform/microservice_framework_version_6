package uk.gov.justice.services.messaging.jms;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWithMemberAsFirstMethodOf;

import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsEnvelopeSenderProducerTest {

    private InjectionPoint adaptorCommandApiInjectionPointA = injectionPointWithMemberAsFirstMethodOf(TestCommandApiAdaptorA.class);
    private InjectionPoint adaptorCommandApiInjectionPointB = injectionPointWithMemberAsFirstMethodOf(TestCommandApiAdaptorB.class);
    private InjectionPoint adaptorQueryApiInjectionPoint = injectionPointWithMemberAsFirstMethodOf(TestQueryApiAdaptor.class);
    private InjectionPoint noAnnotationInjectionPoint = injectionPointWithMemberAsFirstMethodOf(TestNoAnnotation.class);

    @Mock
    private JmsSender jmsSender;

    @Mock
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Spy
    private ComponentNameExtractor componentNameExtractor = new ComponentNameExtractor();

    @InjectMocks
    private JmsEnvelopeSenderProducer jmsEnvelopeSenderProducer;

    @Test
    public void shouldProduceDefaultJmsEnvelopeSender() {

        final JmsEnvelopeSender jmsEnvelopeSender = jmsEnvelopeSenderProducer.createJmsEnvelopeSender(adaptorQueryApiInjectionPoint);

        assertThat(jmsEnvelopeSender, is(instanceOf(DefaultJmsEnvelopeSender.class)));
    }

    @Test
    public void shouldProduceShutteringJmsEnvelopeSender() {

        final JmsEnvelopeSender jmsEnvelopeSender = jmsEnvelopeSenderProducer.createJmsEnvelopeSender(adaptorCommandApiInjectionPointA);

        assertThat(jmsEnvelopeSender, is(instanceOf(ShutteringJmsEnvelopeSender.class)));
    }

    @Test
    public void shouldProduceDefaultJmsEnvelopeSenderWhenNoComponentAnnotation() {

        final JmsEnvelopeSender jmsEnvelopeSender = jmsEnvelopeSenderProducer.createJmsEnvelopeSender(noAnnotationInjectionPoint);

        assertThat(jmsEnvelopeSender, is(instanceOf(DefaultJmsEnvelopeSender.class)));
    }

    @Test
    public void shouldReturnTheDifferentJmsEnvelopeSenderForTwoInjectionPoints() throws Exception {

        final JmsEnvelopeSender jmsEnvelopeSender_1 = jmsEnvelopeSenderProducer.createJmsEnvelopeSender(adaptorCommandApiInjectionPointA);
        final JmsEnvelopeSender jmsEnvelopeSender_2 = jmsEnvelopeSenderProducer.createJmsEnvelopeSender(adaptorCommandApiInjectionPointB);

        assertThat(jmsEnvelopeSender_1, is(not(sameInstance(jmsEnvelopeSender_2))));
    }

    @Test
    public void shouldReturnDifferentJmsEnvelopeSenderForCommandApiAndQueryApiInjectionPoints() throws Exception {

        final JmsEnvelopeSender jmsEnvelopeSender_1 = jmsEnvelopeSenderProducer.createJmsEnvelopeSender(adaptorCommandApiInjectionPointA);
        final JmsEnvelopeSender jmsEnvelopeSender_2 = jmsEnvelopeSenderProducer.createJmsEnvelopeSender(adaptorQueryApiInjectionPoint);

        assertThat(jmsEnvelopeSender_1, is(not(sameInstance(jmsEnvelopeSender_2))));
    }

    @FrameworkComponent(COMMAND_API)
    public static class TestCommandApiAdaptorA {

        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {
        }
    }

    @FrameworkComponent(COMMAND_API)
    public static class TestCommandApiAdaptorB {

        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {
        }
    }

    @FrameworkComponent(QUERY_API)
    public static class TestQueryApiAdaptor {

        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {
        }
    }

    public static class TestNoAnnotation {

        @Inject
        JmsEnvelopeSender jmsEnvelopeSender;

        public void dummyMethod() {
        }
    }
}