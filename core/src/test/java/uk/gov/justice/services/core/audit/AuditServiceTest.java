package uk.gov.justice.services.core.audit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private AuditService  auditService;

    @Test
    public void shouldAudit() throws Exception {

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        auditService.audit(jsonEnvelope);

        verify(auditClient, times(1)).auditEntry(jsonEnvelope);
    }
}
