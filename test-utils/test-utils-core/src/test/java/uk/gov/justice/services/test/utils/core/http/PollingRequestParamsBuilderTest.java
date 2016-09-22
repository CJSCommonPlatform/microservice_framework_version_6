package uk.gov.justice.services.test.utils.core.http;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_DELAY_MILLIS;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_RETRY_COUNT;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Test;


public class PollingRequestParamsBuilderTest {

    @Test
    public void shouldCreatePollingRequestParametersWithUrlAndMediaTypeAndDefaultValues() throws Exception {

        final String url = "a url";
        final String mediaType = "the media type";
        final Response anyResponse = mock(Response.class);

        final PollingRequestParamsBuilder pollingRequestParamsBuilder = new PollingRequestParamsBuilder(url, mediaType);

        final PollingRequestParams pollingRequestParams = pollingRequestParamsBuilder.build();

        assertThat(pollingRequestParams.getUrl(), is(url));
        assertThat(pollingRequestParams.getMediaType(), is(mediaType));
        assertThat(pollingRequestParams.getDelayInMillis(), is(DEFAULT_DELAY_MILLIS));
        assertThat(pollingRequestParams.getRetryCount(), is(DEFAULT_RETRY_COUNT));
        assertThat(pollingRequestParams.getHeaders().isEmpty(), is(true));
        assertThat(pollingRequestParams.getResultCondition().test("don't care"), is(true));
    }

    @Test
    public void shouldCreatePollingRequestParametersWithUrlOverriddenValues() throws Exception {

        final String url = "a url";
        final String mediaType = "the media type";
        final long delayInMillis = 192837L;
        final int retryCount = 9238;
        final Predicate<Response> responsePredicate = e -> true;
        final Predicate<String> resultPredicate = e -> true;

        final Map<String, Object> headers = new HashMap<>();
        headers.put("name", singletonList("value"));

        final PollingRequestParamsBuilder pollingRequestParamsBuilder = new PollingRequestParamsBuilder(url, mediaType)
                .withDelayInMillis(delayInMillis)
                .withHeaders(headers)
                .withRetryCount(retryCount)
                .withResultCondition(resultPredicate);

        final PollingRequestParams pollingRequestParams = pollingRequestParamsBuilder.build();

        assertThat(pollingRequestParams.getUrl(), is(url));
        assertThat(pollingRequestParams.getMediaType(), is(mediaType));
        assertThat(pollingRequestParams.getDelayInMillis(), is(delayInMillis));
        assertThat(pollingRequestParams.getRetryCount(), is(retryCount));
        assertThat(pollingRequestParams.getHeaders().size(), is(1));
        assertThat(pollingRequestParams.getHeaders().get(0), is(headers.get(0)));
        assertThat(pollingRequestParams.getResultCondition(), is(resultPredicate));
    }

    @Test
    public void shouldUseAnExpectedJsonValuesConditionIfValuesArePassedIn() throws Exception {

        final String url = "a url";
        final String mediaType = "the media type";
        final HashMap<String, String> values = new HashMap<>();
        values.put("key", "value");

        final ExpectedJsonValuesResultCondition expectedJsonValuesResultCondition
                = new ExpectedJsonValuesResultCondition(values);

        final PollingRequestParamsBuilder pollingRequestParamsBuilder = new PollingRequestParamsBuilder(url, mediaType);

        final PollingRequestParams pollingRequestParams = pollingRequestParamsBuilder
                .withExpectedJsonResponseValues(values)
                .build();

        assertThat(pollingRequestParams.getUrl(), is(url));
        assertThat(pollingRequestParams.getMediaType(), is(mediaType));
        assertThat(pollingRequestParams.getResultCondition(), is(expectedJsonValuesResultCondition));
    }

    @Test
    public void shouldAddAHeaderToTheHeadersMapIfPassedIn() throws Exception {

        final String url = "a url";
        final String mediaType = "the media type";

        final PollingRequestParamsBuilder pollingRequestParamsBuilder = new PollingRequestParamsBuilder(url, mediaType);

        final PollingRequestParams pollingRequestParams = pollingRequestParamsBuilder
                .withHeader("name", "value")
                .build();

        assertThat(pollingRequestParams.getUrl(), is(url));
        assertThat(pollingRequestParams.getMediaType(), is(mediaType));

        final MultivaluedMap<String, Object> headers = pollingRequestParams.getHeaders();
        assertThat(headers.size(), is(1));
        assertThat(headers.get("name").get(0), is("value"));
    }
}
