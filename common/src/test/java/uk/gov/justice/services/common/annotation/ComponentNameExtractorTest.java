package uk.gov.justice.services.common.annotation;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWith;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.CustomAdapter;
import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComponentNameExtractorTest {

    private static final String FIELD_NAME = "field";

    @InjectMocks
    private ComponentNameExtractor componentNameExtractor;

    @Test
    public void shouldReturnFieldLevelComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                ServiceComponentFieldLevelAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("COMMAND_CONTROLLER"));
    }

    @Test
    public void shouldReturnClassLevelComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                ServiceComponentClassLevelAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("COMMAND_HANDLER"));
    }

    @Test
    public void shouldReturnClassLevelAdaptorComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                AdapterAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("EVENT_LISTENER"));
    }

    @Test
    public void shouldReturnClassLevelCustomAdaptorComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                CustomAdapterAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("CUSTOM_ADAPTER"));
    }

    @Test
    public void shouldReturnClassLevelDirectAdaptorComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                DirectAdapterAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("QUERY_API"));
    }

    @Test
    public void shouldReturnClassLevelComponentForMethodInjectionPoint() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                ServiceComponentClassLevelAnnotationMethod.class.getDeclaredMethods()[0])),
                equalTo("QUERY_API"));
    }

    @Test
    public void shouldReturnClassLevelFrameworkComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                FrameworkComponentClassLevelAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("CUSTOM_NAME_ABC"));
    }

    @Test
    public void shouldReturnFieldLevelFrameworkComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                FrameworkComponentFieldLevelAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("CUSTOM_NAME_BCD"));
    }

    @Test
    public void shouldReturnClassLevelCustomServiceComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                CustomServiceClassLevelAnnotation.class.getDeclaredMethods()[0])),
                equalTo("CUSTOM_SERVICE_NAME"));
    }

    @Test
    public void shouldReturnFieldLevelCustomServiceComponent() throws NoSuchFieldException {
        assertThat(componentNameExtractor.componentFrom(injectionPointWith(
                CustomServiceFieldLevelAnnotation.class.getDeclaredField(FIELD_NAME))),
                equalTo("CUSTOM_SERVICE_NAME"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMissingComponentAnnotation() throws NoSuchFieldException {
        componentNameExtractor.componentFrom(injectionPointWith(NoAnnotation.class.getDeclaredField(FIELD_NAME)));
    }

    @Test
    public void shouldReturnTrueForClassWithComponentAnnotation() throws Exception {

        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(ServiceComponentClassLevelAnnotation.class.getDeclaredField(FIELD_NAME))), is(true));
        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(ServiceComponentClassLevelAnnotationMethod.class.getDeclaredMethods()[0])), is(true));
        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(AdapterAnnotation.class.getDeclaredField(FIELD_NAME))), is(true));
        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(DirectAdapterAnnotation.class.getDeclaredField(FIELD_NAME))), is(true));
        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(CustomAdapterAnnotation.class.getDeclaredField(FIELD_NAME))), is(true));
        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(FrameworkComponentClassLevelAnnotation.class.getDeclaredField(FIELD_NAME))), is(true));
        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(CustomServiceClassLevelAnnotation.class.getDeclaredMethods()[0])), is(true));
    }

    @Test
    public void shouldReturnFalseForClassWithNoComponentAnnotation() throws Exception {
        assertThat(componentNameExtractor.hasComponentAnnotation(injectionPointWith(NoAnnotation.class.getDeclaredField(FIELD_NAME))), is(false));
    }

    public static class ServiceComponentFieldLevelAnnotation {

        @Inject
        @ServiceComponent(Component.COMMAND_CONTROLLER)
        Object field;

    }

    @ServiceComponent(Component.COMMAND_HANDLER)
    public static class ServiceComponentClassLevelAnnotation {

        @Inject
        Object field;

    }

    @ServiceComponent(Component.QUERY_API)
    public static class ServiceComponentClassLevelAnnotationMethod {

        @Inject
        public void test(Object field) {

        }

    }

    @Adapter(Component.EVENT_LISTENER)
    public static class AdapterAnnotation {

        @Inject
        Object field;

    }

    @DirectAdapter(value = Component.QUERY_API)
    public static class DirectAdapterAnnotation {

        @Inject
        Object field;

    }

    @CustomAdapter("CUSTOM_ADAPTER")
    public static class CustomAdapterAnnotation {

        @Inject
        Object field;

    }

    @FrameworkComponent("CUSTOM_NAME_ABC")
    public static class FrameworkComponentClassLevelAnnotation {

        @Inject
        Object field;

    }

    public static class FrameworkComponentFieldLevelAnnotation {

        @Inject
        @FrameworkComponent("CUSTOM_NAME_BCD")
        Object field;

    }

    @CustomServiceComponent("CUSTOM_SERVICE_NAME")
    public static class CustomServiceClassLevelAnnotation {

        @Inject
        public void test(Object field) {

        }

    }

    public static class CustomServiceFieldLevelAnnotation {

        @Inject
        @CustomServiceComponent("CUSTOM_SERVICE_NAME")
        Object field;
    }

    public static class NoAnnotation {

        @Inject
        Object field;

    }
}