package uk.gov.justice.services.messaging.logging;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.NAME;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.logging.HttpMessageLoggerHelper.toHttpHeaderTrace;

import uk.gov.justice.services.common.http.HeaderConstants;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HttpMessageLoggerHelperTest {

    private static final String CORRELATION_ID_VALUE = randomUUID().toString();
    private static final String SESSION_ID_VALUE = randomUUID().toString();
    private static final String NAME_VALUE = randomUUID().toString();
    private static final String USER_ID_VALUE = randomUUID().toString();
    private static final String ID_VALUE = randomUUID().toString();

    @Mock
    MultivaluedMap<String, String> map;

    @Mock
    HttpHeaders httpHeaders;

    @Mock
    MediaType mediaType;

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(HttpMessageLoggerHelper.class);
    }

    @Test
    public void shouldContainAllHttpHeaderConstants() {
        when(httpHeaders.getMediaType()).thenReturn(mediaType);
        when(mediaType.toString()).thenReturn("media.type.test");

        when(httpHeaders.getHeaderString(CLIENT_CORRELATION_ID)).thenReturn(CORRELATION_ID_VALUE);
        when(httpHeaders.getHeaderString(SESSION_ID)).thenReturn(SESSION_ID_VALUE);
        when(httpHeaders.getHeaderString(NAME)).thenReturn(NAME_VALUE);
        when(httpHeaders.getHeaderString(USER_ID)).thenReturn(USER_ID_VALUE);
        when(httpHeaders.getHeaderString(ID)).thenReturn(ID_VALUE);
        when(httpHeaders.getRequestHeaders()).thenReturn(map);

        when(map.containsKey(ID)).thenReturn(true);
        when(map.containsKey(CLIENT_CORRELATION_ID)).thenReturn(true);
        when(map.containsKey(SESSION_ID)).thenReturn(true);
        when(map.containsKey(NAME)).thenReturn(true);
        when(map.containsKey(USER_ID)).thenReturn(true);

        with(toHttpHeaderTrace(httpHeaders))
                .assertEquals(HeaderConstants.ID, ID_VALUE)
                .assertEquals(HeaderConstants.CLIENT_CORRELATION_ID, CORRELATION_ID_VALUE)
                .assertEquals(HeaderConstants.NAME, NAME_VALUE)
                .assertEquals(HeaderConstants.SESSION_ID, SESSION_ID_VALUE)
                .assertEquals(HeaderConstants.USER_ID, USER_ID_VALUE);
    }

    @Test
    public void shouldNotFailDueToMissingFields() {
        when(httpHeaders.getMediaType()).thenReturn(mediaType);
        when(mediaType.toString()).thenReturn("media.type.test");

        when(httpHeaders.getHeaderString(ID)).thenReturn(ID_VALUE);
        when(httpHeaders.getHeaderString(CLIENT_CORRELATION_ID)).thenReturn(CORRELATION_ID_VALUE);
        when(httpHeaders.getHeaderString(NAME)).thenReturn(NAME_VALUE);
        when(httpHeaders.getRequestHeaders()).thenReturn(map);

        when(map.containsKey(ID)).thenReturn(true);
        when(map.containsKey(CLIENT_CORRELATION_ID)).thenReturn(true);
        when(map.containsKey(NAME)).thenReturn(true);

        with(toHttpHeaderTrace(httpHeaders))
                .assertEquals(HeaderConstants.ID, ID_VALUE)
                .assertEquals(HeaderConstants.CLIENT_CORRELATION_ID, CORRELATION_ID_VALUE)
                .assertEquals(HeaderConstants.NAME, NAME_VALUE)
                .assertNotDefined(HeaderConstants.SESSION_ID)
                .assertNotDefined(HeaderConstants.USER_ID);
    }


}