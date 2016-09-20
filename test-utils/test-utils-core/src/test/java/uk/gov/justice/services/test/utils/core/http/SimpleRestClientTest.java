package uk.gov.justice.services.test.utils.core.http;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SimpleRestClientTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private SimpleRestClient simpleRestClient;

    @Test
    public void shouldGetTheResponseAndReturnAsAString() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final MultivaluedHashMap<String, Object> headers = someHeaders();

        final PollingRequestParams pollingRequestParams = mock(PollingRequestParams.class);
        final Response response = mock(Response.class);
        final String result = "{\"some\": \"json\"}";
        final Predicate<Response> alwaysTrueResponseCondition = mock(Predicate.class);
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);

        when(pollingRequestParams.getUrl()).thenReturn(url);
        when(pollingRequestParams.getMediaType()).thenReturn(mediaType);
        when(pollingRequestParams.getHeaders()).thenReturn(headers);
        when(pollingRequestParams.getResponseCondition()).thenReturn(alwaysTrueResponseCondition);
        when(pollingRequestParams.getResultCondition()).thenReturn(alwaysTrueResultCondition);

        when(restClient.query(
                url,
                mediaType,
                headers)).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(result);

        when(alwaysTrueResponseCondition.test(response)).thenReturn(true);
        when(alwaysTrueResultCondition.test(result)).thenReturn(true);

        final Optional<Response> responseOptional = simpleRestClient.get(pollingRequestParams);

        assertThat(responseOptional.isPresent(), is(true));
        assertThat(responseOptional.get(), is(response));
    }

    @Test  @SuppressWarnings("unchecked")
    public void shouldReturnEmptyIfTheResponseConditionFails() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final MultivaluedHashMap<String, Object> headers = someHeaders();

        final PollingRequestParams pollingRequestParams = mock(PollingRequestParams.class);
        final Response response = mock(Response.class);
        final String result = "{\"some\": \"json\"}";
        final Predicate<Response> alwaysFalseResponseCondition = mock(Predicate.class);
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);

        when(pollingRequestParams.getUrl()).thenReturn(url);
        when(pollingRequestParams.getMediaType()).thenReturn(mediaType);
        when(pollingRequestParams.getHeaders()).thenReturn(headers);
        when(pollingRequestParams.getResponseCondition()).thenReturn(alwaysFalseResponseCondition);
        when(pollingRequestParams.getResultCondition()).thenReturn(alwaysTrueResultCondition);

        when(restClient.query(
                url,
                mediaType,
                headers)).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(result);

        when(alwaysFalseResponseCondition.test(response)).thenReturn(false);
        when(alwaysTrueResultCondition.test(result)).thenReturn(true);

        assertThat(simpleRestClient.get(pollingRequestParams).isPresent(), is(false));
    }

    @Test  @SuppressWarnings("unchecked")
    public void shouldReturnEmptyIfTheResultConditionFails() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final MultivaluedHashMap<String, Object> headers = someHeaders();

        final PollingRequestParams pollingRequestParams = mock(PollingRequestParams.class);
        final Response response = mock(Response.class);
        final String result = "{\"some\": \"json\"}";
        final Predicate<Response> alwaysTrueResponseCondition = mock(Predicate.class);
        final Predicate<String>  alwaysFalseResultCondition = mock(Predicate.class);

        when(pollingRequestParams.getUrl()).thenReturn(url);
        when(pollingRequestParams.getMediaType()).thenReturn(mediaType);
        when(pollingRequestParams.getHeaders()).thenReturn(headers);
        when(pollingRequestParams.getResponseCondition()).thenReturn(alwaysTrueResponseCondition);
        when(pollingRequestParams.getResultCondition()).thenReturn(alwaysFalseResultCondition);

        when(restClient.query(
                url,
                mediaType,
                headers)).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(result);

        when(alwaysTrueResponseCondition.test(response)).thenReturn(true);
        when(alwaysFalseResultCondition.test(result)).thenReturn(false);

        assertThat(simpleRestClient.get(pollingRequestParams).isPresent(), is(false));
    }

    private MultivaluedHashMap<String, Object> someHeaders() {
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put("name", singletonList("value"));

        return headers;
    }
}
