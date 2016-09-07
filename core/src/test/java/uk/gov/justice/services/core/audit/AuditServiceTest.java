package uk.gov.justice.services.core.audit;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    private static final String ACTION_NAME = "test.action";

    @Mock
    private AuditClient auditClient;

    @Mock
    private Logger logger;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Metadata metadata;

    @InjectMocks
    private AuditService auditService;

    @Before
    public void setup() {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(ACTION_NAME);
    }

    @Test
    public void shouldAuditWithDefaultEmptyBlacklist() throws Exception {
        initialisePattern("");
        auditService.audit(jsonEnvelope);

        verify(auditClient, times(1)).auditEntry(jsonEnvelope);
    }

    @Test
    public void shouldAuditNonBlacklistedAction() throws Exception {
        initialisePattern(".*\\.action");
        when(metadata.name()).thenReturn("some-action");

        auditService.audit(jsonEnvelope);

        verify(auditClient, times(1)).auditEntry(jsonEnvelope);
    }

    @Test
    public void shouldNotAuditBlacklistedAction() {
        initialisePattern(".*\\.action");

        auditService.audit(jsonEnvelope);

        verify(logger, times(1)).info("Skipping auditing of action test.action due to configured blacklist pattern .*\\.action.");
        verify(auditClient, never()).auditEntry(jsonEnvelope);
    }

    private void initialisePattern(final String pattern) {
        auditService.auditBlacklist = pattern;
        auditService.initialise();
    }
}
