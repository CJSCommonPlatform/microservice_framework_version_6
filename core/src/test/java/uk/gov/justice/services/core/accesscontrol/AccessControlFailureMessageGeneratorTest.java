package uk.gov.justice.services.core.accesscontrol;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlFailureMessageGeneratorTest {

    @InjectMocks
    private AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    @Test
    public void shouldGenerateAnErrorMessageWithUserIdEnvelopeNameAndFailureReason()
                    throws Exception {

        final String jsonEnvelopeString = "the: jsonEnvelope";
        final String reason = "reason";

        final AccessControlViolation accessControlViolation = new AccessControlViolation(reason);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(jsonEnvelope.toString()).thenReturn(jsonEnvelopeString);

        final String errorMessage = accessControlFailureMessageGenerator
                        .errorMessageFrom(jsonEnvelope, accessControlViolation);

        assertThat(errorMessage, is("Access Control failed for json envelope 'the: jsonEnvelope'. Reason: reason"));
    }
}
