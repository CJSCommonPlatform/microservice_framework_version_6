package uk.gov.justice.services.messaging.logging;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.NAME;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;
import static uk.gov.justice.services.messaging.logging.HttpMessageLoggerHelper.toHttpHeaderTrace;

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
    private static final String CONTENT_TYPE_VALUE = "media.type.content";
    private static final String ACCEPT_VALUE = "media.type.response";

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private MediaType mediaType;

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(HttpMessageLoggerHelper.class);
    }

    @SuppressWarnings("unchecked")
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
        map.add(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        map.add(ACCEPT, ACCEPT_VALUE);

        when(httpHeaders.getRequestHeaders()).thenReturn(map);

        assertThat(toHttpHeaderTrace(httpHeaders), isJson(
                allOf(
                        withJsonPath("$." + METADATA + "." + ID, equalTo(ID_VALUE)),
                        withJsonPath("$." + METADATA + "." + CLIENT_CORRELATION_ID, equalTo(CORRELATION_ID_VALUE)),
                        withJsonPath("$." + METADATA + "." + SESSION_ID, equalTo(SESSION_ID_VALUE)),
                        withJsonPath("$." + METADATA + "." + NAME, equalTo(NAME_VALUE)),
                        withJsonPath("$." + METADATA + "." + USER_ID, equalTo(USER_ID_VALUE)),
                        withJsonPath("$." + CONTENT_TYPE, equalTo(CONTENT_TYPE_VALUE)),
                        withJsonPath("$." + ACCEPT, equalTo(ACCEPT_VALUE)))
        ));
    }

    @Test
    public void shouldNotFailDueToMissingFields() {

        final MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        map.add(CLIENT_CORRELATION_ID, CORRELATION_ID_VALUE);
        map.add(NAME, NAME_VALUE);
        map.add(ID, ID_VALUE);

        when(httpHeaders.getRequestHeaders()).thenReturn(map);

        assertThat(toHttpHeaderTrace(httpHeaders), isJson(
                allOf(
                        withJsonPath("$." + METADATA + "." + ID, equalTo(ID_VALUE)),
                        withJsonPath("$." + METADATA + "." + CLIENT_CORRELATION_ID, equalTo(CORRELATION_ID_VALUE)),
                        withJsonPath("$." + METADATA + "." + NAME, equalTo(NAME_VALUE)),
                        hasNoJsonPath("$." + METADATA + "." + SESSION_ID),
                        hasNoJsonPath("$." + METADATA + "." + USER_ID))));
    }
}