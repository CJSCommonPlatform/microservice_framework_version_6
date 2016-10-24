package uk.gov.justice.services.test.utils.core.helper;

import static com.google.common.collect.ImmutableMap.of;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.UUID;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException expectedException = none();

    private PollingRestClientHelper poll;

    @Before
    public void setUp() {
        when(restClient.query(anyString(), anyString(), any())).thenReturn(response);

        poll = new PollingRestClientHelper(restClient, requestParams(REQUEST_URL, MEDIA_TYPE).withHeaders(HEADERS).build())
                .with().logging()
                .with().pollInterval(100, MILLISECONDS)
                .with().timeout(2, SECONDS);
    }

    @Test
    public void shouldPollUntilResponseMatchesExpectedPayload() {
        final String payloadValue = STRING.next();
        when(response.readEntity(String.class))
                .thenReturn("{}")
                .thenReturn(createObjectBuilder().add("payloadKey", payloadValue).build().toString());

        poll.until(
                payload()
                        .isJson(allOf(
                                withJsonPath("$.payloadKey", equalTo(payloadValue))
                                )
                        )
        );

        verify(restClient, times(2)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(2)).readEntity(String.class);
    }

    @Test
    public void shouldPollUntilResponseMatchesExpectedStatus() {
        when(response.getStatus())
                .thenReturn(NOT_FOUND.getStatusCode())
                .thenReturn(ACCEPTED.getStatusCode());

        poll.until(
                status().is(ACCEPTED)
        );

        verify(restClient, times(2)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(2)).getStatus();
    }

    @Test
    public void shouldPollUntilResponseMatchesExpectedPayloadAndStatus() {
        final String payloadValue = STRING.next();
        when(response.readEntity(String.class))
                .thenReturn("{}")
                .thenReturn(createObjectBuilder().add("payloadKey", payloadValue).build().toString());
        when(response.getStatus())
                .thenReturn(NOT_FOUND.getStatusCode())
                .thenReturn(ACCEPTED.getStatusCode());

        poll.until(
                payload()
                        .isJson(allOf(
                                withJsonPath("$.payloadKey", equalTo(payloadValue))
                                )
                        ),
                status().is(ACCEPTED)
        );

        verify(restClient, times(2)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(2)).readEntity(String.class);
        verify(response, times(2)).getStatus();
    }

    @Test
    public void shouldPollUntilResponseStatusIs202Ignoring404Or403Status() {
        when(response.getStatus())
                .thenReturn(NOT_FOUND.getStatusCode())
                .thenReturn(FORBIDDEN.getStatusCode())
                .thenReturn(ACCEPTED.getStatusCode());

        poll
                .ignoring(
                        status().is(NOT_FOUND)
                )
                .and()
                .ignoring(
                        status().is(FORBIDDEN)
                )
                .until(
                        status().is(ACCEPTED)
                );

        verify(restClient, times(3)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(3)).getStatus();
    }

    @Test
    public void shouldPollUntilResponsePayloadHasRequiredNumberOfItems() {
        final String userId1 = UUID.next().toString();
        final String userId2 = UUID.next().toString();

        final JsonArray emptyEvents = createArrayBuilder().build();
        final JsonArrayBuilder eventsWith1Item = createArrayBuilder()
                .add(createObjectBuilder().add("userId", userId1).build());
        final JsonArrayBuilder eventsWith2Items = createArrayBuilder()
                .add(createObjectBuilder().add("userId", userId1).build())
                .add(createObjectBuilder().add("userId", userId2).build());

        when(response.getStatus())
                .thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class))
                .thenReturn(createObjectBuilder().add("events", emptyEvents).build().toString())
                .thenReturn(createObjectBuilder().add("events", eventsWith1Item).build().toString())
                .thenReturn(createObjectBuilder().add("events", eventsWith2Items).build().toString());

        poll
                .ignoring(
                        status().is(OK),
                        payload()
                                .isJson(
                                        withJsonPath("$.events", hasSize(lessThan(2)))
                                )
                )
                .until(
                        status().is(OK),
                        payload()
                                .isJson(allOf(
                                        withJsonPath("$.events", hasSize(2)),
                                        withJsonPath("$.events[0].userId", is(userId1)),
                                        withJsonPath("$.events[1].userId", is(userId2))
                                        )
                                )
                );

        verify(restClient, times(3)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(3)).getStatus();
    }

    @Test
    public void shouldFailFastWhenResponseIsNotPartOfIgnoreOrExpectedData() {
        expectedException.expect(AssertionError.class);

        when(response.getStatus())
                .thenReturn(NOT_FOUND.getStatusCode())
                .thenReturn(FORBIDDEN.getStatusCode())
                .thenReturn(ACCEPTED.getStatusCode());

        poll
                .ignoring(
                        status().is(NOT_FOUND)
                )
                .until(
                        status().is(ACCEPTED)
                );

        verify(restClient, times(2)).query(REQUEST_URL, MEDIA_TYPE, new MultivaluedHashMap<>(HEADERS));
        verify(response, times(2)).getStatus();
    }

}