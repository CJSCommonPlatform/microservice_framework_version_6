package uk.gov.justice.services.common.converter.jms;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JmsConverterExceptionTest {

    private static final String MESSAGE = "Test Messsage";
    private static final Exception CAUSE = new Exception(MESSAGE);

    @Test
    public void shouldReturnValidException() {
        JmsConverterException jmsConverterException = new JmsConverterException(MESSAGE, CAUSE);
        assertThat(jmsConverterException.getMessage(), equalTo(MESSAGE));
        assertThat(jmsConverterException.getCause(), equalTo(CAUSE));
    }

}