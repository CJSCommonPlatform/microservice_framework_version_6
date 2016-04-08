package uk.gov.justice.services.core.extension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import javax.enterprise.inject.spi.Bean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link ServiceComponentFoundEvent} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceComponentFoundEventTest {

    @Mock
    private Bean<Object> bean;

    private ServiceComponentFoundEvent event;

    @Before
    public void setup() {
        event = new ServiceComponentFoundEvent(COMMAND_API, bean);
    }

    @Test
    public void shouldReturnBean() {
        assertThat(event.getHandlerBean(), equalTo(bean));
    }

    @Test
    public void shouldReturnCommandApiComponent() {
        assertThat(event.getComponent(), equalTo(COMMAND_API));
    }
}
