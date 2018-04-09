package uk.gov.justice.raml.jms.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JmsEndpointGeneratorExceptionTest {

    private static final String MESSAGE = "Test Messsage";
    private static final Exception CAUSE = new Exception(MESSAGE);

    @Test
    public void shouldCreateAValidJmsEndpointGeneratorException() throws Exception {
        final JmsEndpointGeneratorException jmsEndpointGeneratorException = new JmsEndpointGeneratorException(MESSAGE, CAUSE);
        assertThat(jmsEndpointGeneratorException.getMessage(), equalTo(MESSAGE));
        assertThat(jmsEndpointGeneratorException.getCause(), equalTo(CAUSE));
        assertThat(jmsEndpointGeneratorException, instanceOf(RuntimeException.class));
    }

}