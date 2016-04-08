package uk.gov.justice.services.core.extension;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.core.annotation.Event;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationScannerTest {

    private static final String TEST_EVENT_NAME = "Test-Event";

    @Mock
    private AfterDeploymentValidation afterDeploymentValidation;

    @Mock
    private ProcessAnnotatedType processAnnotatedType;

    @Mock
    private AnnotatedType annotatedType;

    @Mock
    private BeanManager beanManager;

    @Mock
    private ServiceComponentFoundEvent serviceComponentFoundEvent;

    @Mock
    private EventFoundEvent eventFoundEvent;

    @Mock
    private Bean<Object> beanMockCommandApiHandler;

    @Mock
    private Bean<Object> beanMockCommandController;

    @Mock
    private Bean<Object> beanMockCommandHandler;

    @Mock
    private Bean<Object> beanMockRemoteQueryApi;

    @Mock
    private Bean<Object> beanMockDummy;

    private AnnotationScanner annotationScanner;

    @Before
    public void setup() {
        annotationScanner = new AnnotationScanner();

        doReturn(TestCommandApiHandler.class).when(beanMockCommandApiHandler).getBeanClass();
        doReturn(TestCommandController.class).when(beanMockCommandController).getBeanClass();
        doReturn(TestCommandHandler.class).when(beanMockCommandHandler).getBeanClass();
        doReturn(TestRemoteQueryApiHandler.class).when(beanMockRemoteQueryApi).getBeanClass();
        doReturn(Object.class).when(beanMockDummy).getBeanClass();
    }

    @Test
    public void shouldFireCommandApiFoundEventWithCommandApi() throws Exception {
        verifyIfServiceComponentFoundEventFiredWith(beanMockCommandApiHandler);
    }

    @Test
    public void shouldFireCommandControllerFoundEventWithCommandController() throws Exception {
        verifyIfServiceComponentFoundEventFiredWith(beanMockCommandController);
    }

    @Test
    public void shouldFireCommandHandlerFoundEventWithCommandHandler() throws Exception {
        verifyIfServiceComponentFoundEventFiredWith(beanMockCommandHandler);
    }

    @Test
    public void shouldFireRemoteQueryApiHandlerFoundEventWithRemoteQueryApi() throws Exception {
        verifyIfRemoteServiceComponentFoundEventFiredWith(beanMockRemoteQueryApi);
    }

    @Test
    public void shouldFireEventFoundEventWithTestEvent() throws Exception {
        verifyIfEventFoundEventFiredWith(processAnnotatedType);
    }

    @Test
    public void shouldNotFireAnyEventWithNoHandler() throws Exception {
        mockBeanManagerGetBeansWith(beanMockDummy);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager, never()).fireEvent(any());
    }

    @SuppressWarnings("serial")
    private void mockBeanManagerGetBeansWith(Bean<Object> handler) {
        doReturn(new HashSet<Bean<Object>>() {
            {
                add(handler);
            }
        }).when(beanManager).getBeans(any(), any());
    }

    private void mockProcessAnnotatedType() {
        doReturn(annotatedType).when(processAnnotatedType).getAnnotatedType();
        doReturn(true).when(annotatedType).isAnnotationPresent(Event.class);
        doReturn(TestEvent.class).when(annotatedType).getJavaClass();
        doReturn(TestEvent.class.getAnnotation(Event.class)).when(annotatedType).getAnnotation(Event.class);
    }

    private void verifyIfServiceComponentFoundEventFiredWith(Bean<Object> handler) {
        ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        mockBeanManagerGetBeansWith(handler);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(ServiceComponentFoundEvent.class));
    }

    private void verifyIfRemoteServiceComponentFoundEventFiredWith(Bean<Object> handler) {
        ArgumentCaptor<RemoteServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(RemoteServiceComponentFoundEvent.class);
        mockBeanManagerGetBeansWith(handler);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(RemoteServiceComponentFoundEvent.class));
    }

    private void verifyIfEventFoundEventFiredWith(ProcessAnnotatedType processAnnotatedType) {
        ArgumentCaptor<EventFoundEvent> captor = ArgumentCaptor.forClass(EventFoundEvent.class);
        mockProcessAnnotatedType();

        annotationScanner.processAnnotatedType(processAnnotatedType);
        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(EventFoundEvent.class));
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandApiHandler {
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandController {
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler {
    }

    @Remote
    @ServiceComponent(QUERY_API)
    public static class TestRemoteQueryApiHandler {
    }

    @Event(TEST_EVENT_NAME)
    public static class TestEvent {
    }
}
