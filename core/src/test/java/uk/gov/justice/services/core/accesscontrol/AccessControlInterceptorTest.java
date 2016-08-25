package uk.gov.justice.services.core.accesscontrol;

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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlInterceptorTest {

    private static final int ACCESS_CONTROL_PRIORITY = 6000;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    AccessControlService accessControlService;

    @Mock
    AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    @InjectMocks
    AccessControlInterceptor accessControlInterceptor;

    private InjectionPoint adaptorCommandLocal = new TestInjectionPoint(TestCommandLocal.class);
    private InjectionPoint adaptorCommandRemote = new TestInjectionPoint(TestCommandRemote.class);

    @Test
    public void shouldApplyAccessControlToInputIfLocalComponent() throws Exception {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(interceptorContext.injectionPoint()).thenReturn(adaptorCommandLocal);
        when(interceptorContext.inputEnvelope()).thenReturn(jsonEnvelope);
        when(accessControlService.checkAccessControl(jsonEnvelope)).thenReturn(Optional.empty());

        accessControlInterceptor.process(interceptorContext, interceptorChain);

        verify(accessControlService).checkAccessControl(jsonEnvelope);
    }

    @Test
    public void shouldNotApplyAccessControlToInputIfRemoteComponent() throws Exception {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);

        when(interceptorContext.injectionPoint()).thenReturn(adaptorCommandRemote);

        accessControlInterceptor.process(interceptorContext, interceptorChain);

        verifyZeroInteractions(accessControlService);
    }

    @Test
    public void shouldReturnAccessControlPriority() throws Exception {
        assertThat(accessControlInterceptor.priority(), is(ACCESS_CONTROL_PRIORITY));
    }

    @Test
    public void shouldThrowAccessControlViolationExceptionIfAccessControlFailsForInput() throws Exception {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final AccessControlViolation accessControlViolation = new AccessControlViolation("reason");

        when(interceptorContext.injectionPoint()).thenReturn(adaptorCommandLocal);
        when(interceptorContext.inputEnvelope()).thenReturn(jsonEnvelope);
        when(accessControlService.checkAccessControl(jsonEnvelope)).thenReturn(Optional.of(accessControlViolation));
        when(accessControlFailureMessageGenerator.errorMessageFrom(jsonEnvelope, accessControlViolation)).thenReturn("Error message");

        exception.expect(AccessControlViolationException.class);
        exception.expectMessage("Error message");

        accessControlInterceptor.process(interceptorContext, interceptorChain);
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