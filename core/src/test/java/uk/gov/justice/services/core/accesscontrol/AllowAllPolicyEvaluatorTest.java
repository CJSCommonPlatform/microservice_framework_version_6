package uk.gov.justice.services.core.accesscontrol;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AllowAllPolicyEvaluatorTest {

    @InjectMocks
    private AllowAllPolicyEvaluator allowAllAccessController;

    @Test
    public void shouldAllowAllAccess() throws Exception {

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        assertThat(allowAllAccessController.checkAccessPolicyFor("command", jsonEnvelope).isPresent(), is(false));
    }
}
