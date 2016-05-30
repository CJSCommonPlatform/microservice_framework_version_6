package uk.gov.justice.services.messaging.logging;

import com.jayway.jsonassert.JsonAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.http.HeaderConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.messaging.logging.ResponseLoggerHelper.toResponseTrace;

@RunWith(MockitoJUnitRunner.class)
public class ResponseLoggerHelperTest {

    private static final String CPP_ID = "145e0eca-f0a6-40b7-8f91-2a2709ab2a8a";
    private static final int STATUS_CODE = 202;
    private static final String MEDIA_TYPE = "context.command.dosomething";

    @Mock
    private MediaType mediaType;

    @Mock
    private Response response;

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(ResponseLoggerHelper.class);
    }

    @Test
    public void shouldPrintResponseParameters() {
        when(response.getHeaderString(ID)).thenReturn(CPP_ID);
        when(response.getMediaType()).thenReturn(mediaType);
        when(response.getStatus()).thenReturn(STATUS_CODE);
        when(mediaType.getType()).thenReturn(MEDIA_TYPE);

        JsonAssert.with(toResponseTrace(response))
                .assertEquals("MediaType", MEDIA_TYPE)
                .assertEquals(ID, CPP_ID)
                .assertEquals("ResponseCode", STATUS_CODE);
    }

    @Test
    public void shouldNotPrintMissingResponseParameters() {
        when(response.getHeaderString(ID)).thenReturn(CPP_ID);
        when(response.getMediaType()).thenReturn(null);
        when(response.getStatus()).thenReturn(404);

        JsonAssert.with(toResponseTrace(response))
                .assertNotDefined("MediaType")
                .assertEquals(ID, CPP_ID)
                .assertEquals("ResponseCode", 404);
    }

}