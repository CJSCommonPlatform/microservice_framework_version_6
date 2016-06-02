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
import javax.ws.rs.core.MultivaluedHashMap;
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

        final MultivaluedMap<String, String> map = new MultivaluedHashMap<>();

        map.add(CLIENT_CORRELATION_ID, CORRELATION_ID_VALUE);
        map.add(SESSION_ID, SESSION_ID_VALUE);
        map.add(NAME, NAME_VALUE);
        map.add(USER_ID, USER_ID_VALUE);
        map.add(ID, ID_VALUE);
        map.add(HttpHeaders.CONTENT_TYPE, "media.type.test");

        when(httpHeaders.getRequestHeaders()).thenReturn(map);

        with(toHttpHeaderTrace(httpHeaders))
                .assertEquals(HeaderConstants.ID, ID_VALUE)
                .assertEquals(HeaderConstants.CLIENT_CORRELATION_ID, CORRELATION_ID_VALUE)
                .assertEquals(HeaderConstants.NAME, NAME_VALUE)
                .assertEquals(HeaderConstants.SESSION_ID, SESSION_ID_VALUE)
                .assertEquals(HeaderConstants.USER_ID, USER_ID_VALUE);
    }

    @Test
    public void shouldNotFailDueToMissingFields() {

        final MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        map.add(CLIENT_CORRELATION_ID, CORRELATION_ID_VALUE);
        map.add(NAME, NAME_VALUE);
        map.add(ID, ID_VALUE);

        when(httpHeaders.getRequestHeaders()).thenReturn(map);

        with(toHttpHeaderTrace(httpHeaders))
                .assertEquals(HeaderConstants.ID, ID_VALUE)
                .assertEquals(HeaderConstants.CLIENT_CORRELATION_ID, CORRELATION_ID_VALUE)
                .assertEquals(HeaderConstants.NAME, NAME_VALUE)
                .assertNotDefined(HeaderConstants.SESSION_ID)
                .assertNotDefined(HeaderConstants.USER_ID);
    }


}