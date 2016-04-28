package uk.gov.justice.services.core.sender;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.annotation.exception.MissingAnnotationException;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.core.jms.JmsSenderFactory;

import java.lang.reflect.Member;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private Sender commandApiSender;

    @Mock
    private JmsSenderFactory jmsSenderFactory;

    @Mock
    private JmsSender commandControllerJmsSender;

    @Mock
    private JmsSender commandHandlerJmsSender;

    private SenderProducer senderProducer;

    @Before
    public void setup() {
        senderProducer = new SenderProducer();
        senderProducer.componentDestination = new ComponentDestination();
        senderProducer.jmsSenderFactory = jmsSenderFactory;
    }

    @Test
    public void shouldReturnNewSender() throws Exception {
        mockInjectionPoint(commandControllerInjectionPoint1, commandControllerMember1, TestCommandController1.class);
        when(jmsSenderFactory.createJmsSender(COMMAND_HANDLER)).thenReturn(commandHandlerJmsSender);

        final Sender sender = senderProducer.produce(commandControllerInjectionPoint1);

        assertThat(sender, notNullValue());
        verify(jmsSenderFactory, times(1)).createJmsSender(COMMAND_HANDLER);
    }

    @Test
    public void shouldReturnExistingSender() throws Exception {
        mockInjectionPoint(commandControllerInjectionPoint1, commandControllerMember1, TestCommandController1.class);
        mockInjectionPoint(commandControllerInjectionPoint2, commandControllerMember2, TestCommandController2.class);
        when(jmsSenderFactory.createJmsSender(COMMAND_CONTROLLER)).thenReturn(commandControllerJmsSender);
        when(jmsSenderFactory.createJmsSender(COMMAND_HANDLER)).thenReturn(commandHandlerJmsSender);

        final Sender sender = senderProducer.produce(commandControllerInjectionPoint1);
        assertThat(sender, notNullValue());

        final Sender anotherSender = senderProducer.produce(commandControllerInjectionPoint2);
        assertThat(anotherSender, sameInstance(sender));

        verify(jmsSenderFactory, times(1)).createJmsSender(COMMAND_HANDLER);
    }

    @Test
    public void shouldReturnADifferentSender() throws Exception {
        mockInjectionPoint(commandApiInjectionPoint, commandApiMember, TestCommandApi.class);
        mockInjectionPoint(commandControllerInjectionPoint2, commandControllerMember2, TestCommandController2.class);
        when(jmsSenderFactory.createJmsSender(COMMAND_CONTROLLER)).thenReturn(commandControllerJmsSender);
        when(jmsSenderFactory.createJmsSender(COMMAND_HANDLER)).thenReturn(commandHandlerJmsSender);

        final Sender sender = senderProducer.produce(commandApiInjectionPoint);

        assertThat(sender, notNullValue());

        final Sender anotherSender = senderProducer.produce(commandControllerInjectionPoint2);

        assertThat(anotherSender, not(equalTo(sender)));
        verify(jmsSenderFactory, times(1)).createJmsSender(COMMAND_CONTROLLER);
        verify(jmsSenderFactory, times(1)).createJmsSender(COMMAND_HANDLER);
    }

    @Test(expected = MissingAnnotationException.class)
    public void shouldThrowExceptionWithInvalidHandler() throws Exception {
        mockInjectionPoint(invalidInjectionPoint, invalidMember, TestInvalidHandler.class);

        senderProducer.produce(invalidInjectionPoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalidComponent() throws Exception {
        mockInjectionPoint(commandHandlerInjectionPoint, commandHandlerMember, TestCommandHandler.class);

        senderProducer.produce(commandHandlerInjectionPoint);
    }

    private void mockInjectionPoint(final InjectionPoint injectionPoint, final Member member, final Class<?> declaringClass) {
        when(injectionPoint.getMember()).thenReturn(member);
        doReturn(declaringClass).when(member).getDeclaringClass();
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