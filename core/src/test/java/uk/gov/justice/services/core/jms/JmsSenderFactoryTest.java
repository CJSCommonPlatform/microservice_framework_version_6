package uk.gov.justice.services.core.jms;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class JmsSenderFactoryTest {

    @Mock
    private JmsDestinations jmsDestinations;

    @Mock
    private JmsEnvelopeSender jmsEnvelopeSender;

    @InjectMocks
    private JmsSenderFactory jmsSenderFactory;

    @Test
    public void shouldReturnNewJmsSender() throws Exception {

        final Component componentDestination = COMMAND_API;

        final Sender sender = jmsSenderFactory.createSender(componentDestination);

        assertThat(sender, notNullValue());
        assertThat(sender, is(instanceOf(JmsSender.class)));

        final JmsSender jmsSender = (JmsSender) sender;

        assertThat(jmsSender.jmsDestinations, is(sameInstance(jmsDestinations)));
        assertThat(jmsSender.jmsEnvelopeSender, is(sameInstance(jmsEnvelopeSender)));
        assertThat(jmsSender.destinationComponent, is(componentDestination));

        assertThat(jmsSender.logger, is(LoggerFactory.getLogger(JmsSender.class)));
    }

}
