package uk.gov.justice.raml.jms.core;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JmsEndpointGeneratorExceptionTest {

    private static final String MESSAGE = "Test Messsage";
    private static final Exception CAUSE = new Exception(MESSAGE);

    @Test
    public void shouldCreateAValidJmsEndpointGeneratorException() throws Exception {
        JmsEndpointGeneratorException jmsEndpointGeneratorException = new JmsEndpointGeneratorException(MESSAGE, CAUSE);
        assertThat(jmsEndpointGeneratorException.getMessage(), equalTo(MESSAGE));
        assertThat(jmsEndpointGeneratorException.getCause(), equalTo(CAUSE));
    }

}