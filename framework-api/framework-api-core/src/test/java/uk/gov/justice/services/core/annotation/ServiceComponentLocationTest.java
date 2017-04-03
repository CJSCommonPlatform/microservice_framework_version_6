package uk.gov.justice.services.core.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.REMOTE;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;
import static uk.gov.justice.services.core.util.MemberInjectionPoint.injectionPointWith;

import javax.inject.Inject;

import org.junit.Test;

/**
 * Unit tests for the {@link ServiceComponentLocation} class.
 */
public class ServiceComponentLocationTest {

    private static final String FIELD = "field";

    @Test
    public void shouldIdentifyLocalComponent() {
        assertThat(componentLocationFrom(TestLocalComponent.class), equalTo(LOCAL));
    }

    @Test
    public void shouldIdentifyRemoteComponent() {
        assertThat(componentLocationFrom(TestRemoteComponent.class), equalTo(REMOTE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnLocalForAdapterAnnotatedInjectionPoint() throws Exception {
        assertThat(componentLocationFrom(injectionPointWith(AdapterComponent.class.getDeclaredField(FIELD))), equalTo(LOCAL));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnLocalForCustomAdapterAnnotatedInjectionPoint() throws Exception {
        assertThat(componentLocationFrom(injectionPointWith(CustomAdapterComponent.class.getDeclaredField(FIELD))), equalTo(LOCAL));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnLocalForFrameworkComponentAnnotatedInjectionPoint() throws Exception {
        assertThat(componentLocationFrom(injectionPointWith(FrameworkComponentRemote.class.getDeclaredField(FIELD))), equalTo(REMOTE));
    }

    private static class TestLocalComponent {

    }

    @Remote
    private static class TestRemoteComponent {

    }

    @Adapter(QUERY_API)
    private static class AdapterComponent {

        @Inject
        Object field;
    }

    @CustomAdapter("CUSTOM_ADAPTER")
    private static class CustomAdapterComponent {

        @Inject
        Object field;
    }

    @FrameworkComponent("Framework-Component")
    private static class FrameworkComponentRemote {

        @Inject
        Object field;
    }
}
