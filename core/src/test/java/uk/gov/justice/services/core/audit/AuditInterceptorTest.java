package uk.gov.justice.services.core.audit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.core.util.TestInjectionPoint;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditInterceptorTest {

    private static final int AUDIT_PRIORITY = 2000;

    @Mock
    private AuditService auditService;

    @Mock
    private InterceptorContext inputInterceptorContext;

    @Mock
    private InterceptorContext outputInterceptorContext;

    @Mock
    private InterceptorChain interceptorChain;

    @InjectMocks
    AuditInterceptor auditInterceptor;

    private InjectionPoint adaptorCommandLocal = new TestInjectionPoint(AuditInterceptorTest.TestCommandLocal.class);
    private InjectionPoint adaptorCommandRemote = new TestInjectionPoint(AuditInterceptorTest.TestCommandRemote.class);

    @Test
    public void shouldApplyAccessControlToInputIfLocalComponent() throws Exception {
        final JsonEnvelope inputJsonEnvelope = mock(JsonEnvelope.class);
        when(inputInterceptorContext.injectionPoint()).thenReturn(adaptorCommandLocal);
        when(inputInterceptorContext.inputEnvelope()).thenReturn(inputJsonEnvelope);

        final JsonEnvelope outputJsonEnvelope = mock(JsonEnvelope.class);
        when(interceptorChain.processNext(inputInterceptorContext)).thenReturn(outputInterceptorContext);
        when(outputInterceptorContext.outputEnvelope()).thenReturn(Optional.of(outputJsonEnvelope));

        auditInterceptor.process(inputInterceptorContext, interceptorChain);

        verify(auditService).audit(inputJsonEnvelope);
        verify(auditService).audit(outputJsonEnvelope);
    }

    @Test
    public void shouldNotApplyAccessControlToInputIfRemoteComponent() throws Exception {
        when(inputInterceptorContext.injectionPoint()).thenReturn(adaptorCommandRemote);

        auditInterceptor.process(inputInterceptorContext, interceptorChain);

        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldReturnAccessControlPriority() throws Exception {
        assertThat(auditInterceptor.priority(), is(AUDIT_PRIORITY));
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