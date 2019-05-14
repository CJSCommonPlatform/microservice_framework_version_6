package uk.gov.justice.services.core.accesscontrol;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlFailureMessageGeneratorTest {

    @InjectMocks
    private AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    @Test
    public void shouldGenerateAnErrorMessageWithEnvelopeIdAndEnvelopeNameAndFailureReasonAndNoUserId()
            throws Exception {

        final String reason = "reason";
        final AccessControlViolation accessControlViolation = new AccessControlViolation(reason);
        final Metadata metadata = mock(Metadata.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(fromString("9132d439-c797-4483-a961-5eb640c55fe7"));
        when(metadata.name()).thenReturn("sjp.query.cases-referred-to-court");

        final String errorMessage = accessControlFailureMessageGenerator
                .errorMessageFrom(jsonEnvelope, accessControlViolation);

        assertThat(errorMessage, is("Access Control failed for json envelope '9132d439-c797-4483-a961-5eb640c55fe7' of type 'sjp.query.cases-referred-to-court'. Reason: reason"));

        verify(metadata, times(0)).userId();
    }
}
