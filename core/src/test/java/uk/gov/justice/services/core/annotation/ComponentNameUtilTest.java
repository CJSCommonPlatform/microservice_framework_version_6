package uk.gov.justice.services.core.annotation;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWith;

import javax.inject.Inject;

import org.junit.Test;

public class ComponentNameUtilTest {

    private static final String FIELD_NAME = "field";

    @Test
    public void shouldReturnFieldLevelComponent() throws NoSuchFieldException {
        assertThat(componentFrom(injectionPointWith(ServiceComponentFieldLevelAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo("COMMAND_CONTROLLER"));
    }

    @Test
    public void shouldReturnClassLevelComponent() throws NoSuchFieldException {
        assertThat(componentFrom(injectionPointWith(ServiceComponentClassLevelAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo("COMMAND_HANDLER"));
    }

    @Test
    public void shouldReturnClassLevelAdaptorComponent() throws NoSuchFieldException {
        assertThat(componentFrom(injectionPointWith(AdapterAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo("EVENT_LISTENER"));
    }

    @Test
    public void shouldReturnClassLevelCustomAdaptorComponent() throws NoSuchFieldException {
        assertThat(componentFrom(injectionPointWith(CustomAdapterAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo("CUSTOM_ADAPTER"));
    }

    @Test
    public void shouldReturnClassLevelComponentForMethodInjectionPoint() throws NoSuchFieldException {
        assertThat(componentFrom(injectionPointWith(MethodAnnotation.class.getDeclaredMethods()[0])), equalTo("QUERY_API"));
    }

    @Test
    public void shouldReturnClassLevelFrameworkComponent() throws NoSuchFieldException {
        assertThat(componentFrom(injectionPointWith(FrameworkComponentClassLevelAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo("CUSTOM_NAME_ABC"));
    }

    @Test
    public void shouldReturnFieldLevelFrameworkComponent() throws NoSuchFieldException {
        assertThat(componentFrom(injectionPointWith(FrameworkComponentFieldLevelAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo("CUSTOM_NAME_BCD"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMissingComponentAnnotation() throws NoSuchFieldException {
        componentFrom(injectionPointWith(NoAnnotation.class.getDeclaredField(FIELD_NAME)));
    }

    public static class ServiceComponentFieldLevelAnnotation {

        @Inject
        @ServiceComponent(COMMAND_CONTROLLER)
        Object field;

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class ServiceComponentClassLevelAnnotation {

        @Inject
        Object field;

    }

    @ServiceComponent(QUERY_API)
    public static class ServiceComponentClassLevelAnnotationMethod {

        @Inject
        public void test(Object field) {

        }

    }

    @Adapter(EVENT_LISTENER)
    public static class AdapterAnnotation {

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

    public static class NoAnnotation {

        @Inject
        Object field;

    }
}