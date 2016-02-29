package uk.gov.justice.services.core.sender;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.HandlerUtilTest;
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
    private InjectionPoint commandAPIInjectionPoint;

    @Mock
    private InjectionPoint commandControllerInjectionPoint1;

    @Mock
    private InjectionPoint commandControllerInjectionPoint2;

    @Mock
    private InjectionPoint commandHandlerInjectionPoint;

    @Mock
    private InjectionPoint invalidInjectionPoint;

    @Mock
    private Member commandAPIMember;

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
    private Sender commandAPISender;

    private SenderProducer senderProducer;

    @Before
    public void setup() {
        senderProducer = new SenderProducer();
        senderProducer.jmsEndpoints = new JmsEndpoints();
        senderProducer.jmsSender = jmsSender;
        senderProducer.componentDestination = new ComponentDestination();

        when(commandAPIInjectionPoint.getMember()).thenReturn(commandAPIMember);
        when(commandControllerInjectionPoint1.getMember()).thenReturn(commandControllerMember1);
        when(commandControllerInjectionPoint2.getMember()).thenReturn(commandControllerMember2);
        when(commandHandlerInjectionPoint.getMember()).thenReturn(commandHandlerMember);
        when(invalidInjectionPoint.getMember()).thenReturn(invalidMember);

        doReturn(TestCommandAPI.class).when(commandAPIMember).getDeclaringClass();
        doReturn(TestCommandController1.class).when(commandControllerMember1).getDeclaringClass();
        doReturn(TestCommandController1.class).when(commandControllerMember2).getDeclaringClass();
        doReturn(TestCommandHandler.class).when(commandHandlerMember).getDeclaringClass();
        doReturn(HandlerUtilTest.InvalidHandler.class).when(invalidMember).getDeclaringClass();

    }

    @Test
    public void shouldReturnSender() throws Exception {
        Sender sender = senderProducer.produce(commandControllerInjectionPoint1);
        assertThat(sender, notNullValue());
    }

    @Test
    public void shouldReturnExistingSender() throws Exception {
        Sender sender = senderProducer.produce(commandControllerInjectionPoint1);
        assertThat(sender, notNullValue());

        Sender anotherSender = senderProducer.produce(commandControllerInjectionPoint2);
        assertThat(anotherSender, equalTo(sender));
    }

    @Test
    public void shouldReturnADifferentSender() throws Exception {
        Sender sender = senderProducer.produce(commandAPIInjectionPoint);
        assertThat(sender, notNullValue());

        Sender anotherSender = senderProducer.produce(commandControllerInjectionPoint2);
        assertThat(anotherSender, not(equalTo(sender)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidHandler() throws Exception {
        senderProducer.produce(invalidInjectionPoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidComponent() throws Exception {
        senderProducer.produce(commandHandlerInjectionPoint);
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandAPI {
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