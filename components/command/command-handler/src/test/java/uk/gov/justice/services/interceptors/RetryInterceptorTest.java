package uk.gov.justice.services.interceptors;

import static java.lang.Integer.MAX_VALUE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.eventsourcing.repository.core.exception.OptimisticLockingRetryException;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class RetryInterceptorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Logger logger;

    @Mock
    private InterceptorChain interceptorChain;

    @InjectMocks
    private RetryInterceptor retryInterceptor;

    @Test
    public void shouldRetryIfExceptionThrownByDispatcher() throws Exception {
        final InterceptorContext currentContext = interceptorContextWithInput(
                envelope().with(metadataWithRandomUUID("nameABC")).build(), null);
        final InterceptorContext nextInChain = interceptorContextWithInput(null, null);

        when(interceptorChain.processNext(currentContext))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"))
                .thenReturn(nextInChain);

        retryInterceptor.maxRetry = "2";
        retryInterceptor.waitTime = "500";

        assertThat(retryInterceptor.process(currentContext, interceptorChain), is(nextInChain));
    }

    @Test
    public void shouldThrowExceptionIfRetryMaxValueIsExceeded() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final InterceptorContext currentContext = interceptorContextWithInput(
                envelope().with(metadataWithRandomUUID("nameABC")
                        .withStreamId(streamId))
                        .build(), null);

        when(interceptorChain.processNext(currentContext))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"));

        retryInterceptor.maxRetry = "1";
        retryInterceptor.waitTime = "500";

        expectedException.expect(RuntimeException.class);

        retryInterceptor.process(currentContext, interceptorChain);
    }

    @Test
    public void shouldHaveMinPriortity() throws Exception {
        assertThat(retryInterceptor.priority(), is(MAX_VALUE));
    }
}