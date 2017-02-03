package uk.gov.justice.services.core.envelope;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvelopeValidationExceptionHandlerProducerTest {

    @InjectMocks
    private EnvelopeValidationExceptionHandlerProducer producer;

    @Test
    public void shouldProduceRethrowingHandler() {
        producer.handlerClass = "uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler";
        assertThat(producer.envelopeValidationExceptionHandler(), instanceOf(RethrowingValidationExceptionHandler.class));
    }

    @Test
    public void shouldProduceLoggingHandler() {
        producer.handlerClass = "uk.gov.justice.services.core.envelope.LoggingValidationExceptionHandler";
        assertThat(producer.envelopeValidationExceptionHandler(), instanceOf(LoggingValidationExceptionHandler.class));
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfClassDoesNotExist() {
        producer.handlerClass = "uk.gov.justice.services.core.envelope.NonExisting";

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Could not instantiate validation exception handler.");

        producer.envelopeValidationExceptionHandler();

    }

}