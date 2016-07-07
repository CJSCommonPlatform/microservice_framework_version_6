package uk.gov.justice.services.core.extension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.enterprise.inject.spi.Bean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link ProviderFoundEvent} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProviderFoundEventTest {

    @Mock
    private Bean<Object> bean;

    private ProviderFoundEvent event;

    @Before
    public void setup() {
        event = new ProviderFoundEvent(bean);
    }

    @Test
    public void shouldReturnBean() {
        assertThat(event.getBean(), equalTo(bean));
    }
}
