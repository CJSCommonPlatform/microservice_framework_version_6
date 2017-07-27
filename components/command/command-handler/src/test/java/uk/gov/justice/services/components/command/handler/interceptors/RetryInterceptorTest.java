package uk.gov.justice.services.components.command.handler.interceptors;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

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
                envelope().with(metadataWithRandomUUID("nameABC")).build());
        final InterceptorContext nextInChain = interceptorContextWithInput(mock(JsonEnvelope.class));

        when(interceptorChain.processNext(currentContext))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"))
                .thenReturn(nextInChain);

        retryInterceptor.maxRetry = "2";
        retryInterceptor.waitTime = "500";
        retryInterceptor.immediateRetries = "0";

        assertThat(retryInterceptor.process(currentContext, interceptorChain), is(nextInChain));
    }

    @Test
    public void shouldThrowExceptionIfRetryMaxValueIsExceeded() throws Exception {
        final UUID streamId = UUID.randomUUID();
        final JsonEnvelope envelope = envelope().with(metadataWithRandomUUID("nameABC")
                .withStreamId(streamId))
                .build();
        final InterceptorContext currentContext = interceptorContextWithInput(envelope);

        when(interceptorChain.processNext(currentContext))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"));

        retryInterceptor.maxRetry = "1";
        retryInterceptor.waitTime = "500";
        retryInterceptor.immediateRetries = "0";

        expectedException.expect(OptimisticLockingRetryFailedException.class);
        expectedException.expectMessage("Retry count of 1 exceeded for command " + envelope.metadata().asJsonObject());

        retryInterceptor.process(currentContext, interceptorChain);
    }

    @Test
    public void shouldRetryStraightAwayForThreeAttemptsThenWaitBeforeRetry() throws Exception {
        final InterceptorContext currentContext = interceptorContextWithInput(
                envelope().with(metadataWithRandomUUID("nameABC")).build());
        final InterceptorContext nextInChain = interceptorContextWithInput(mock(JsonEnvelope.class));

        when(interceptorChain.processNext(currentContext))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"))
                .thenThrow(new OptimisticLockingRetryException("Locking Error"))
                .thenReturn(nextInChain);

        retryInterceptor.maxRetry = "5";
        retryInterceptor.waitTime = "1000";
        retryInterceptor.immediateRetries = "3";

        final long start = currentTimeMillis();
        assertThat(retryInterceptor.process(currentContext, interceptorChain), is(nextInChain));
        final long end = currentTimeMillis();

        final long runTime = end - start;
        assertThat(runTime > 999, is(true));
        assertThat(runTime < 1999, is(true));
    }
}