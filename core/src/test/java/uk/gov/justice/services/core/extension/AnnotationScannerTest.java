package uk.gov.justice.services.core.extension;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.HashSet;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationScannerTest {

    @Mock
    private AfterDeploymentValidation afterDeploymentValidation;

    @Mock
    private BeanManager beanManager;

    @Mock
    private ServiceComponentFoundEvent serviceComponentFoundEvent;

    @Mock
    private Bean<TestCommandAPIHandler> beanMockCommandAPIHandler;

    @Mock
    private Bean<TestCommandController> beanMockCommandController;

    @Mock
    private Bean<TestCommandHandler> beanMockCommandHandler;

    @Mock
    private Bean<Object> beanMockDummy;

    private AnnotationScanner annotationScanner;

    @Before
    public void setup() {
        annotationScanner = new AnnotationScanner();

        doReturn(TestCommandAPIHandler.class).when(beanMockCommandAPIHandler).getBeanClass();
        doReturn(TestCommandController.class).when(beanMockCommandController).getBeanClass();
        doReturn(TestCommandHandler.class).when(beanMockCommandHandler).getBeanClass();
        doReturn(Object.class).when(beanMockDummy).getBeanClass();
    }

    @Test
    public void shouldFireCommandAPIFoundEventWithCommandAPI() throws Exception {
        ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        doReturn(new HashSet<Bean>() {{
            add(beanMockCommandAPIHandler);
        }}).when(beanManager).getBeans(any(), any());

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.instanceOf(ServiceComponentFoundEvent.class));
    }

    @Test
    public void shouldFireCommandControllerFoundEventWithCommandController() throws Exception {
        ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        doReturn(new HashSet<Bean>() {{
            add(beanMockCommandController);
        }}).when(beanManager).getBeans(any(), any());

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.instanceOf(ServiceComponentFoundEvent.class));
    }

    @Test
    public void shouldFireCommandHandlerFoundEventWithCommandHandler() throws Exception {
        ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        doReturn(new HashSet<Bean>() {{
            add(beanMockCommandHandler);
        }}).when(beanManager).getBeans(any(), any());

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.instanceOf(ServiceComponentFoundEvent.class));
    }

    @Test
    public void shouldNotFireAnyEventWithNoHandler() throws Exception {
        doReturn(new HashSet<Bean>() {{
            add(beanMockDummy);
        }}).when(beanManager).getBeans(any(), any());

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager, never()).fireEvent(any());
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandAPIHandler {
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandController {
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler {
    }
}