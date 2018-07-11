package uk.gov.justice.services.core.extension;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
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
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.extension.util.EmptyAfterDeploymentValidation;
import uk.gov.justice.services.core.extension.util.TestBean;

import java.util.HashSet;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceComponentScannerTest {

    private static final String TEST_EVENT_NAME = "Test-Event";

    private static final AfterDeploymentValidation NOT_USED_AFTER_DEPLOYMENT_VALIDATION = new EmptyAfterDeploymentValidation();

    @Mock
    private ProcessAnnotatedType processAnnotatedType;

    @Mock
    private AnnotatedType annotatedType;

    @Mock
    private BeanManager beanManager;

    private ServiceComponentScanner serviceComponentScanner;

    @Before
    public void setup() {
        serviceComponentScanner = new ServiceComponentScanner();
    }

    @Test
    public void shouldFireCommandApiFoundEventWithCommandApi() throws Exception {
        verifyIfServiceComponentFoundEventFiredWith(TestBean.of(TestCommandApiHandler.class));
    }

    @Test
    public void shouldFireCommandControllerFoundEventWithCommandController() throws Exception {
        verifyIfServiceComponentFoundEventFiredWith(TestBean.of(TestCommandController.class));
    }

    @Test
    public void shouldFireCommandHandlerFoundEventWithCommandHandler() throws Exception {

        verifyIfServiceComponentFoundEventFiredWith(TestBean.of(TestCommandHandler.class));
    }

    @Test
    public void shouldFireRemoteQueryApiHandlerFoundEventWithRemoteQueryApi() throws Exception {

        verifyIfRemoteServiceComponentFoundEventFiredWith(TestBean.of(TestRemoteQueryApiHandler.class));
    }

    @Test
    public void shouldFireServiceComponentFoundEventForFrameworkComponentAnnotation() throws Exception {

        verifyIfServiceComponentFoundEventFiredWith(TestBean.of(TestFrameworkComponent.class));
    }

    @Test
    public void shouldFireServiceComponentFoundEventForCustomServiceComponentAnnotation() throws Exception {

        verifyIfServiceComponentFoundEventFiredWith(TestBean.of(TestCustomServiceComponent.class));
    }

    @Test
    public void shouldFireEventFoundEventWithTestEvent() throws Exception {
        verifyIfEventFoundEventFiredWith(processAnnotatedType);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotFireAnyEventWithNoHandler() throws Exception {

        mockBeanManagerGetBeansWith(TestBean.of(Object.class));

        serviceComponentScanner.afterDeploymentValidation(NOT_USED_AFTER_DEPLOYMENT_VALIDATION, beanManager);

        verify(beanManager, never()).fireEvent(any());
    }

    @Test
    public void shouldFireEventForDirectHandlerIfCorrespondingDirectAdapterExists() throws Exception {
        final ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);

        doReturn(new HashSet<Bean<Object>>(asList(TestBean.of(TestDirectQueryViewAdapter.class), TestBean.of(TestDirectQueryApiHandler.class)))).when(beanManager).getBeans(any(), any());
        doReturn(new HashSet<Bean<Object>>(asList(TestBean.of(TestDirectQueryViewAdapter.class)))).when(beanManager).getBeans(SynchronousDirectAdapter.class);

        serviceComponentScanner.afterDeploymentValidation(NOT_USED_AFTER_DEPLOYMENT_VALIDATION, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(ServiceComponentFoundEvent.class));
        assertThat(captor.getValue().getHandlerBean().getBeanClass(), equalTo(TestDirectQueryApiHandler.class));
        assertThat(captor.getValue().getLocation(), equalTo(ServiceComponentLocation.REMOTE));
    }

    @Test
    public void shouldNotFireEventForDirectComponentIfCorrespondingDirectAdapterDoesNotExist() throws Exception {

        doReturn(new HashSet<Bean<Object>>(asList(
                TestBean.of(OtherTestDirectAdapter.class),
                TestBean.of(TestDirectQueryApiHandler.class))))
                .when(beanManager).getBeans(any(), any());
        doReturn(new HashSet<Bean<Object>>(asList(TestBean.of(OtherTestDirectAdapter.class))))
                .when(beanManager).getBeans(SynchronousDirectAdapter.class);

        serviceComponentScanner.afterDeploymentValidation(NOT_USED_AFTER_DEPLOYMENT_VALIDATION, beanManager);

        verify(beanManager, never()).fireEvent(any());
    }

    @SuppressWarnings("serial")
    private void mockBeanManagerGetBeansWith(final Bean<Object>... handlers) {
        doReturn(new HashSet<>(asList(handlers))).when(beanManager).getBeans(any(), any());
    }

    private void mockProcessAnnotatedType() {
        doReturn(annotatedType).when(processAnnotatedType).getAnnotatedType();
        doReturn(true).when(annotatedType).isAnnotationPresent(Event.class);
        doReturn(TestEvent.class).when(annotatedType).getJavaClass();
        doReturn(TestEvent.class.getAnnotation(Event.class)).when(annotatedType).getAnnotation(Event.class);
    }

    @SuppressWarnings("unchecked")
    private void verifyIfServiceComponentFoundEventFiredWith(final Bean<Object> handler) {
        final ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        mockBeanManagerGetBeansWith(handler);

        serviceComponentScanner.afterDeploymentValidation(NOT_USED_AFTER_DEPLOYMENT_VALIDATION, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(ServiceComponentFoundEvent.class));
        assertThat(captor.getValue().getLocation(), equalTo(ServiceComponentLocation.LOCAL));
    }

    @SuppressWarnings("unchecked")
    private void verifyIfRemoteServiceComponentFoundEventFiredWith(final Bean<Object> handler) {
        final ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        mockBeanManagerGetBeansWith(handler);

        serviceComponentScanner.afterDeploymentValidation(NOT_USED_AFTER_DEPLOYMENT_VALIDATION, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getAllValues(), hasSize(1));
        assertThat(captor.getValue(), instanceOf(ServiceComponentFoundEvent.class));
        assertThat(captor.getValue().getLocation(), equalTo(ServiceComponentLocation.REMOTE));
    }

    @SuppressWarnings("unchecked")
    private void verifyIfEventFoundEventFiredWith(final ProcessAnnotatedType processAnnotatedType) {
        final ArgumentCaptor<EventFoundEvent> captor = ArgumentCaptor.forClass(EventFoundEvent.class);
        mockProcessAnnotatedType();

        serviceComponentScanner.processAnnotatedType(processAnnotatedType);
        serviceComponentScanner.afterDeploymentValidation(NOT_USED_AFTER_DEPLOYMENT_VALIDATION, beanManager);

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

    @FrameworkComponent("COMPONENT_ABC")
    public static class TestFrameworkComponent {
    }

    @CustomServiceComponent("COMPONENT_XYZ")
    public static class TestCustomServiceComponent {
    }

    @Remote
    @ServiceComponent(QUERY_API)
    public static class TestRemoteQueryApiHandler {
    }

    @Direct(target = QUERY_VIEW)
    @ServiceComponent(QUERY_API)
    public static class TestDirectQueryApiHandler {
    }

    @DirectAdapter("QUERY_VIEW")
    public static class TestDirectQueryViewAdapter {

    }

    @Direct(target = "COMPONENT_B")
    @FrameworkComponent("COMPONENT_A")
    public static class TestDirectComponentAHandler {
    }

    @DirectAdapter("COMPONENT_B")
    public static class TestDirectComponentBAdapter {

    }

    @Remote
    @FrameworkComponent("COMPONENT_A")
    public static class TestRemoteComponentAHandler {
    }

    @Event(TEST_EVENT_NAME)
    public static class TestEvent {
    }

    @DirectAdapter("OTHER")
    public static class OtherTestDirectAdapter {

    }


}
