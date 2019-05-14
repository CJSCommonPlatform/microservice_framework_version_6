package uk.gov.justice.services.messaging.jms;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EnvelopeSenderSelectorTest {

    @Mock
    private JmsSender jmsSender;

    @Mock
    private ShutteringStoreSender shutteringStoreSender;

    @InjectMocks
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Test
    public void shouldGetTheJmsSenderByDefault() throws Exception {

        assertThat(envelopeSenderSelector.getEnvelopeSender(), is(jmsSender));
    }

    @Test
    public void shouldGetTheShutteringStoreSenderIfShutteredIsSetToTrue() throws Exception {

        assertThat(envelopeSenderSelector.getEnvelopeSender(), is(jmsSender));

        envelopeSenderSelector.setShuttered(true);
        assertThat(envelopeSenderSelector.getEnvelopeSender(), is(shutteringStoreSender));

        envelopeSenderSelector.setShuttered(false);
        assertThat(envelopeSenderSelector.getEnvelopeSender(), is(jmsSender));
    }
}
