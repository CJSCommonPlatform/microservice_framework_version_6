package uk.gov.justice.services.core.accesscontrol;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.accesscontrol.AccessControlService.ACCESS_CONTROL_DISABLED_PROPERTY;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlServiceTest {

    @Mock
    private PolicyEvaluator policyEvaluator;

    @InjectMocks
    private AccessControlService accessControlService;

    @After
    public void resetSystemProperty() {
        System.clearProperty(ACCESS_CONTROL_DISABLED_PROPERTY);
    }

    @Test
    public void shouldDelegateTheAccessControlLogicToTheAccessController() throws Exception {

        assertThat(System.getProperty(ACCESS_CONTROL_DISABLED_PROPERTY), is(nullValue()));

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Optional<AccessControlViolation> accessControlViolation =
                        of(mock(AccessControlViolation.class));

        when(policyEvaluator.checkAccessPolicyFor(jsonEnvelope)).thenReturn(accessControlViolation);

        assertThat(accessControlService.checkAccessControl(jsonEnvelope),
                        is(sameInstance(accessControlViolation)));
    }

    @Test
    public void shouldIgnoreAccessControlIfTheAccessControlDisabledPropertyIsTrue() throws Exception {

        System.setProperty(ACCESS_CONTROL_DISABLED_PROPERTY, "true");

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Optional<AccessControlViolation> accessControlViolation =
                        accessControlService.checkAccessControl(jsonEnvelope);

        assertThat(accessControlViolation.isPresent(), is(false));

        verifyZeroInteractions(policyEvaluator);
    }

    @Test
    public void shouldUseAccessControlIfTheAccessControlDisabledPropertyIsFalse() throws Exception {

        System.setProperty(ACCESS_CONTROL_DISABLED_PROPERTY, "false");

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Optional<AccessControlViolation> accessControlViolation =
                        of(mock(AccessControlViolation.class));

        when(policyEvaluator.checkAccessPolicyFor(jsonEnvelope)).thenReturn(accessControlViolation);

        assertThat(accessControlService.checkAccessControl(jsonEnvelope),
                        is(sameInstance(accessControlViolation)));
    }
}
