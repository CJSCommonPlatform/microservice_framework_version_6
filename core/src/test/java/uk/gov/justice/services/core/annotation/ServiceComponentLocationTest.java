package uk.gov.justice.services.core.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.REMOTE;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import org.junit.Test;

/**
 * Unit tests for the {@link ServiceComponentLocation} class.
 */
public class ServiceComponentLocationTest {

    @Test
    public void shouldIdentifyLocalComponent() {
        assertThat(componentLocationFrom(TestLocalComponent.class), equalTo(LOCAL));
    }

    @Test
    public void shouldIdentifyRemoteComponent() {
        assertThat(componentLocationFrom(TestRemoteComponent.class), equalTo(REMOTE));
    }

    private static class TestLocalComponent {

    }

    @Remote
    private static class TestRemoteComponent {

    }
}
