package uk.gov.justice.services.core.sender;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.core.jms.JmsSender;

import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Member;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@RunWith(MockitoJUnitRunner.class)
public class SenderProducerTest {

    @Mock
    private InjectionPoint commandApiInjectionPoint;

    @Mock
    private InjectionPoint commandControllerInjectionPoint1;

    @Mock
    private InjectionPoint commandControllerInjectionPoint2;

    @Mock
    private InjectionPoint commandHandlerInjectionPoint;

    @Mock
    private InjectionPoint invalidInjectionPoint;

    @Mock
    private Member commandApiMember;

    @Mock
    private Member commandControllerMember1;

    @Mock
    private Member commandControllerMember2;

    @Mock
    private Member commandHandlerMember;

    @Mock
    private Member invalidMember;

    @Mock
    private JmsSender jmsSender;

    @Mock
    private Sender commandApiSender;

    private SenderProducer senderProducer;

    private JmsEndpoints jmsEndpoints;

    private Sender sender;

    @Before
    public void setup() {
        jmsEndpoints = new JmsEndpoints();
        senderProducer = new SenderProducer();
        senderProducer.jmsEndpoints = jmsEndpoints;
        senderProducer.jmsSender = jmsSender;
        senderProducer.componentDestination = new ComponentDestination();
    }

    @Test
    public void shouldReturnSender() throws Exception {
        mockInjectionPoint(commandControllerInjectionPoint1, commandControllerMember1, TestCommandController1.class);

        Sender sender = senderProducer.produce(commandControllerInjectionPoint1);

        assertThat(sender, notNullValue());
        assertThat(sender, equalTo(createSender(COMMAND_HANDLER)));
    }

    @Test
    public void shouldReturnExistingSender() throws Exception {
        mockInjectionPoint(commandControllerInjectionPoint1, commandControllerMember1, TestCommandController1.class);
        mockInjectionPoint(commandControllerInjectionPoint2, commandControllerMember2, TestCommandController2.class);

        Sender sender = senderProducer.produce(commandControllerInjectionPoint1);
        assertThat(sender, notNullValue());
        assertThat(sender, equalTo(createSender(COMMAND_HANDLER)));

        Sender anotherSender = senderProducer.produce(commandControllerInjectionPoint2);
        assertThat(anotherSender, equalTo(sender));
    }

    @Test
    public void shouldReturnADifferentSender() throws Exception {
        mockInjectionPoint(commandApiInjectionPoint, commandApiMember, TestCommandApi.class);
        mockInjectionPoint(commandControllerInjectionPoint2, commandControllerMember2, TestCommandController2.class);

        Sender sender = senderProducer.produce(commandApiInjectionPoint);
        assertThat(sender, notNullValue());
        assertThat(sender, equalTo(createSender(COMMAND_CONTROLLER)));

        Sender anotherSender = senderProducer.produce(commandControllerInjectionPoint2);
        assertThat(anotherSender, not(equalTo(sender)));
        assertThat(anotherSender, equalTo(createSender(COMMAND_HANDLER)));
        assertThat(anotherSender, not(equalTo(createSender(COMMAND_CONTROLLER))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidHandler() throws Exception {
        mockInjectionPoint(invalidInjectionPoint, invalidMember, TestInvalidHandler.class);

        senderProducer.produce(invalidInjectionPoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidComponent() throws Exception {
        mockInjectionPoint(commandHandlerInjectionPoint, commandHandlerMember, TestCommandHandler.class);

        senderProducer.produce(commandHandlerInjectionPoint);
    }

    private void mockInjectionPoint(final InjectionPoint injectionPoint, final Member member, final Class declaringClass) {
        when(injectionPoint.getMember()).thenReturn(member);
        doReturn(declaringClass).when(member).getDeclaringClass();
    }

    private Sender createSender(final Component destinationComponent) {
        return new DefaultSender(jmsSender, destinationComponent, jmsEndpoints);
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandApi {
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandController1 {
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandController2 {
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler {
    }

    public static class TestInvalidHandler {
    }
}