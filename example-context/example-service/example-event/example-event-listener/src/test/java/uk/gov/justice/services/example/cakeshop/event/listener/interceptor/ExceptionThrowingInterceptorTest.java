package uk.gov.justice.services.example.cakeshop.event.listener.interceptor;

import static org.junit.Assert.*;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.interceptor.InterceptorChain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionThrowingInterceptorTest {

    private ExceptionThrowingInterceptor interceptor = new ExceptionThrowingInterceptor();

    @Mock
    private InterceptorChain interceptorChain;


    @Test(expected = TestInterceptorException.class)
    public void shouldThrowExceptionWhenNameContainsExceptionalCake() throws Exception {

        interceptor.process(interceptorContextWithInput(
                envelope().with(metadataWithDefaults()).withPayloadOf("Exceptional cake", "name").build(), null), interceptorChain);

    }

    @Test
    public void shouldNotThrowExceptionPayloadDoesNotContainExceptionalCake() {

        interceptor.process(interceptorContextWithInput(
                envelope().with(metadataWithDefaults()).withPayloadOf("Some cake", "name").build(), null), interceptorChain);

        interceptor.process(interceptorContextWithInput(
                envelope().with(metadataWithDefaults()).build(), null), interceptorChain);

    }
}