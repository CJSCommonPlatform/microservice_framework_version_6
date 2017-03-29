package uk.gov.justice.services.core.audit;

import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.copyWithOutput;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.core.interceptor.Target;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Deque;
import java.util.LinkedList;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalAuditInterceptorTest {

    private static final int AUDIT_PRIORITY = 2000;

    @Mock
    private JsonEnvelope inputEnvelope;

    @Mock
    private JsonEnvelope outputEnvelope;

    @InjectMocks
    private LocalAuditInterceptor localAuditInterceptor;

    @Mock
    private AuditService auditService;

    private InterceptorChain interceptorChain;

    @Before
    public void setup() throws Exception {
        final Deque<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(localAuditInterceptor);

        final Target target = context -> copyWithOutput(context, outputEnvelope);

        interceptorChain = new InterceptorChain(interceptors, target);
    }

    @Test
    public void shouldApplyAccessControlToInputIfLocalComponent() throws Exception {
        final InterceptorContext inputContext = interceptorContextWithInput(inputEnvelope);

        interceptorChain.processNext(inputContext);

        verify(auditService).audit(inputEnvelope);
        verify(auditService).audit(outputEnvelope);
    }

    @Adapter(COMMAND_API)
    public static class TestCommandLocal {
        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {

        }
    }

    public static class TestCommandRemote {
        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {

        }
    }
}