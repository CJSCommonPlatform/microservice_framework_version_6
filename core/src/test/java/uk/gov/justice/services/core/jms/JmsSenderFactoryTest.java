package uk.gov.justice.services.core.jms;

import org.junit.Test;
import uk.gov.justice.services.core.annotation.Component;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JmsSenderFactoryTest {

    @Test
    public void shouldReturnNewJmsSender() throws Exception {
        JmsSender jmsSender = new JmsSenderFactory().createJmsSender(Component.COMMAND_API);
        assertThat(jmsSender, notNullValue());
    }

}