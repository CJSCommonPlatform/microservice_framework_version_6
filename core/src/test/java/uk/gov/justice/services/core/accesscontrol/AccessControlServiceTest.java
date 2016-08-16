package uk.gov.justice.services.core.accesscontrol;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.accesscontrol.AccessControlService.ACCESS_CONTROL_DISABLED_PROPERTY;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlServiceTest {

    private static final String ACTION_NAME = "action-name";

    @Mock
    private PolicyEvaluator policyEvaluator;

    @Mock
    private Logger logger;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @InjectMocks
    private AccessControlService accessControlService;

    @Before
    public void setup() {
        final Metadata metadata = mock(Metadata.class);
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(ACTION_NAME);
    }

    @After
    public void resetSystemProperty() {
        System.clearProperty(ACCESS_CONTROL_DISABLED_PROPERTY);
    }

    @Test
    public void shouldDelegateTheAccessControlLogicToTheAccessController() throws Exception {

        assertThat(System.getProperty(ACCESS_CONTROL_DISABLED_PROPERTY), is(nullValue()));

        final Optional<AccessControlViolation> accessControlViolation =
                of(mock(AccessControlViolation.class));

        when(policyEvaluator.checkAccessPolicyFor(jsonEnvelope)).thenReturn(accessControlViolation);

        assertThat(accessControlService.checkAccessControl(jsonEnvelope),
                is(sameInstance(accessControlViolation)));

        assertLogStatement();
    }

    @Test
    public void shouldIgnoreAccessControlIfTheAccessControlDisabledPropertyIsTrue() throws Exception {

        System.setProperty(ACCESS_CONTROL_DISABLED_PROPERTY, "true");

        final Optional<AccessControlViolation> accessControlViolation =
                accessControlService.checkAccessControl(jsonEnvelope);

        assertThat(accessControlViolation.isPresent(), is(false));

        verifyZeroInteractions(policyEvaluator);

        verify(logger).trace("Skipping access control due to configuration");
    }

    @Test
    public void shouldUseAccessControlIfTheAccessControlDisabledPropertyIsFalse() throws Exception {

        System.setProperty(ACCESS_CONTROL_DISABLED_PROPERTY, "false");

        final Optional<AccessControlViolation> accessControlViolation =
                of(mock(AccessControlViolation.class));

        when(policyEvaluator.checkAccessPolicyFor(jsonEnvelope)).thenReturn(accessControlViolation);

        assertThat(accessControlService.checkAccessControl(jsonEnvelope),
                is(sameInstance(accessControlViolation)));

        assertLogStatement();
    }

    private void assertLogStatement() {
        verify(logger).trace("Performing access control for action: {}", ACTION_NAME);
    }
}
