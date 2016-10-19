package uk.gov.justice.services.test.utils.core.helper;

import static com.google.common.collect.ImmutableMap.of;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParamsBuilder.pollingRequestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PollingRestClientHelperTest {

    private static final String REQUEST_URL = STRING.next();
    private static final String MEDIA_TYPE = STRING.next();
    private static final ImmutableMap<String, Object> HEADERS = of("header_key1", "header_value1");

    @Mock
    private RestClient restClient;

    @Mock
    private Response response;

    @Before
    public void setUp() {
        when(restClient.query(anyString(), anyString(), any())).thenReturn(response);
    }

    @Test
    public void shouldPollUntilResponseMatchesExpectedPayload() {
        final String payloadValue = STRING.next();
        when(response.readEntity(String.class)).thenReturn("{}").thenReturn(createObjectBuilder().add("payloadKey", payloadValue).build().toString());

        new PollingRestClientHelper(restClient, pollingRequestParams(REQUEST_URL, MEDIA_TYPE).withHeaders(HEADERS).build())
                .withLogging()
                .until(
                        payload()
                                .isJson(allOf(
                                        withJsonPath("$.payloadKey", equalTo(payloadValue))
                                        )
                                )
                );

        verify(restClient, times(2)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(3)).readEntity(String.class);
    }

    @Test
    public void shouldPollUntilResponseMatchesExpectedStatus() {
        when(response.getStatus()).thenReturn(NOT_FOUND.getStatusCode()).thenReturn(ACCEPTED.getStatusCode());

        new PollingRestClientHelper(restClient, pollingRequestParams(REQUEST_URL, MEDIA_TYPE).withHeaders(HEADERS).build())
                .withLogging()
                .until(
                        status().is(ACCEPTED)
                );

        verify(restClient, times(2)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(3)).getStatus();
    }

    @Test
    @Ignore
    public void shouldPollUntilResponseMatchesExpectedPayloadAndStatus() {
        final String payloadValue = STRING.next();
        when(response.readEntity(String.class)).thenReturn("{}").thenReturn(createObjectBuilder().add("payloadKey", payloadValue).build().toString());
        when(response.getStatus()).thenReturn(NOT_FOUND.getStatusCode()).thenReturn(ACCEPTED.getStatusCode());

        new PollingRestClientHelper(restClient, pollingRequestParams(REQUEST_URL, MEDIA_TYPE).withHeaders(HEADERS).build())
                .withLogging()
                .until(
                        payload()
                                .isJson(allOf(
                                        withJsonPath("$.payloadKey", equalTo(payloadValue))
                                        )
                                )
                );

        verify(restClient, times(2)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(3)).readEntity(String.class);
    }


}