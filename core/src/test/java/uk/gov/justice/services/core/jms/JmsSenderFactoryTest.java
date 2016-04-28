package uk.gov.justice.services.core.jms;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.core.annotation.Component;

import org.junit.Test;

public class JmsSenderFactoryTest {

    @Test
    public void shouldReturnNewJmsSender() throws Exception {
        JmsSender jmsSender = new JmsSenderFactory().createJmsSender(Component.COMMAND_API);
        assertThat(jmsSender, notNullValue());
    }

}