package uk.gov.justice.services.test.utils.core.http;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
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
public class ValidatingRestClientTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private ValidatingRestClient validatingRestClient;

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Test
    public void shouldGetTheResponseAndReturnAsAString() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final int status = 200;
        final MultivaluedHashMap<String, Object> headers = someHeaders();

        final PollingRequestParams pollingRequestParams = mock(PollingRequestParams.class);
        final Response response = mock(Response.class);
        final String responseBody = "{\"some\": \"json\"}";
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);

        when(pollingRequestParams.getUrl()).thenReturn(url);
        when(pollingRequestParams.getMediaType()).thenReturn(mediaType);
        when(pollingRequestParams.getHeaders()).thenReturn(headers);
        when(pollingRequestParams.getExpectedStatus()).thenReturn(of(status));
        when(pollingRequestParams.getResultCondition()).thenReturn(alwaysTrueResultCondition);

        when(restClient.query(
                url,
                mediaType,
                headers)).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(responseBody);
        when(response.getStatus()).thenReturn(status);

        when(alwaysTrueResultCondition.test(responseBody)).thenReturn(true);

        assertThat(alwaysTrueResultCondition.test(responseBody), is(true));

        final Optional<ResponseDetails> responseOptional = validatingRestClient.get(pollingRequestParams);

        assertThat(responseOptional.isPresent(), is(true));
        assertThat(responseOptional.get().getStatus(), is(status));
        assertThat(responseOptional.get().getResponseBody(), is(responseBody));
    }

    @Test  @SuppressWarnings("unchecked")
    public void shouldReturnEmptyIfTheResultConditionFails() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final MultivaluedHashMap<String, Object> headers = someHeaders();

        final PollingRequestParams pollingRequestParams = mock(PollingRequestParams.class);
        final Response response = mock(Response.class);
        final String result = "{\"some\": \"json\"}";
        final int expectedStatus = 200;
        final Predicate<String>  alwaysFalseResultCondition = mock(Predicate.class);

        when(pollingRequestParams.getUrl()).thenReturn(url);
        when(pollingRequestParams.getMediaType()).thenReturn(mediaType);
        when(pollingRequestParams.getHeaders()).thenReturn(headers);
        when(pollingRequestParams.getExpectedStatus()).thenReturn(of(expectedStatus));
        when(pollingRequestParams.getResultCondition()).thenReturn(alwaysFalseResultCondition);

        when(restClient.query(
                url,
                mediaType,
                headers)).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(result);

        when(alwaysFalseResultCondition.test(result)).thenReturn(false);

        assertThat(validatingRestClient.get(pollingRequestParams).isPresent(), is(false));
    }

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Test
    public void shouldNotCheckTheStatusIfItIsAbsent() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final int actualStatus = 202;
        final Optional<Integer> expectedStatus = empty();
        final MultivaluedHashMap<String, Object> headers = someHeaders();

        final PollingRequestParams pollingRequestParams = mock(PollingRequestParams.class);
        final Response response = mock(Response.class);
        final String responseBody = "{\"some\": \"json\"}";
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);

        when(pollingRequestParams.getUrl()).thenReturn(url);
        when(pollingRequestParams.getMediaType()).thenReturn(mediaType);
        when(pollingRequestParams.getHeaders()).thenReturn(headers);
        when(pollingRequestParams.getExpectedStatus()).thenReturn(expectedStatus);
        when(pollingRequestParams.getResultCondition()).thenReturn(alwaysTrueResultCondition);

        when(restClient.query(
                url,
                mediaType,
                headers)).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(responseBody);
        when(response.getStatus()).thenReturn(actualStatus);

        when(alwaysTrueResultCondition.test(responseBody)).thenReturn(true);

        assertThat(alwaysTrueResultCondition.test(responseBody), is(true));

        final Optional<ResponseDetails> responseOptional = validatingRestClient.get(pollingRequestParams);

        assertThat(responseOptional.isPresent(), is(true));
        assertThat(responseOptional.get().getStatus(), is(actualStatus));
        assertThat(responseOptional.get().getResponseBody(), is(responseBody));
    }

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Test
    public void shouldReturnEmptyIfTheExpectedStatusDoesNotMatch() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final int actualStatus = 404;
        final Optional<Integer> expectedStatus = of(200);
        final MultivaluedHashMap<String, Object> headers = someHeaders();

        final PollingRequestParams pollingRequestParams = mock(PollingRequestParams.class);
        final Response response = mock(Response.class);
        final String responseBody = "{\"some\": \"json\"}";
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);

        when(pollingRequestParams.getUrl()).thenReturn(url);
        when(pollingRequestParams.getMediaType()).thenReturn(mediaType);
        when(pollingRequestParams.getHeaders()).thenReturn(headers);
        when(pollingRequestParams.getExpectedStatus()).thenReturn(expectedStatus);
        when(pollingRequestParams.getResultCondition()).thenReturn(alwaysTrueResultCondition);

        when(restClient.query(
                url,
                mediaType,
                headers)).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(responseBody);
        when(response.getStatus()).thenReturn(actualStatus);

        when(alwaysTrueResultCondition.test(responseBody)).thenReturn(true);

        assertThat(alwaysTrueResultCondition.test(responseBody), is(true));

        final Optional<ResponseDetails> responseOptional = validatingRestClient.get(pollingRequestParams);

        assertThat(responseOptional.isPresent(), is(false));
    }


    private MultivaluedHashMap<String, Object> someHeaders() {
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put("name", singletonList("value"));

        return headers;
    }
}
