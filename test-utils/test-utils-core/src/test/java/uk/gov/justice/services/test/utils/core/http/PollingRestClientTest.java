package uk.gov.justice.services.test.utils.core.http;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_DELAY_MILLIS;

import uk.gov.justice.services.test.utils.core.helper.Sleeper;

import java.util.function.Predicate;

import javax.ws.rs.core.Response;


@RunWith(MockitoJUnitRunner.class)
public class PollingRestClientTest {

    @Mock
    private ValidatingRestClient validatingRestClient;

    @Mock
    private Sleeper sleeper;

    @InjectMocks
    private PollingRestClient pollingRestClient;

    @Test @SuppressWarnings("unchecked")
    public void shouldPollUntilASucessfulResponseIsFound() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final int status = 200;
        final String result = "{\"some\": \"json\"}";

        final Response response = mock(Response.class);
        final Predicate<Response>  alwaysTrueResponseCondition = mock(Predicate.class);
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withResponseCondition(alwaysTrueResponseCondition)
                .withResultCondition(alwaysTrueResultCondition)
                .build();

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), of(response));
        when(response.getStatus()).thenReturn(status);
        when(response.readEntity(String.class)).thenReturn(result);
        when(alwaysTrueResponseCondition.test(response)).thenReturn(true);
        when(alwaysTrueResultCondition.test(result)).thenReturn(true);

        assertThat(pollingRestClient.pollUntilExpectedResponse(pollingRequestParams), is(result));

        verify(sleeper, times(2)).sleepFor(DEFAULT_DELAY_MILLIS);
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldThrowAnAssertionExceptionIfNoSuccessfulResponseIsFoundAfterFiveAttempts() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final int retryCount = 5;

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withRetryCount(retryCount)
                .build();

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), empty(), empty(), empty());

        try {
            pollingRestClient.pollUntilExpectedResponse(pollingRequestParams);
        } catch (AssertionError expected) {
            assertThat(expected.getMessage(), is("Failed to get any response from 'http://url.com' after 5 retries"));
        }

        verify(sleeper, times(retryCount)).sleepFor(DEFAULT_DELAY_MILLIS);
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldThrowAnAssertionExceptionIfTheResponseConditionFails() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final int status = 200;
        final String result = "{\"some\": \"json\"}";

        final Response response = mock(Response.class);
        final Predicate<Response>  alwaysFalseResponseCondition = mock(Predicate.class);
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withResponseCondition(alwaysFalseResponseCondition)
                .withResultCondition(alwaysTrueResultCondition)
                .build();

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), of(response));
        when(response.getStatus()).thenReturn(status);
        when(response.readEntity(String.class)).thenReturn(result);
        when(alwaysFalseResponseCondition.test(response)).thenReturn(false);
        when(alwaysTrueResultCondition.test(result)).thenReturn(true);


        try {
            pollingRestClient.pollUntilExpectedResponse(pollingRequestParams);
        } catch (AssertionError expected) {
            assertThat(expected.getMessage(), is("Failed to match response conditions from http://url.com, after 5 attempts, with status code: 200"));
        }

        verify(sleeper, times(2)).sleepFor(DEFAULT_DELAY_MILLIS);
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldThrowAnAssertionExceptionIfTheResultConditionFails() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final int status = 200;
        final String result = "{\"some\": \"json\"}";

        final Response response = mock(Response.class);
        final Predicate<Response>  alwaysTrueResponseCondition = mock(Predicate.class);
        final Predicate<String>  alwaysFalseResultCondition = mock(Predicate.class);

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withResponseCondition(alwaysTrueResponseCondition)
                .withResultCondition(alwaysFalseResultCondition)
                .build();

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), of(response));
        when(response.getStatus()).thenReturn(status);
        when(response.readEntity(String.class)).thenReturn(result);
        when(alwaysTrueResponseCondition.test(response)).thenReturn(true);
        when(alwaysFalseResultCondition.test(result)).thenReturn(false);

        try {
            pollingRestClient.pollUntilExpectedResponse(pollingRequestParams);
        } catch (AssertionError expected) {
            assertThat(expected.getMessage(), is("Failed to match result conditions from http://url.com, after 5 attempts, with result: " + result));
        }

        verify(sleeper, times(2)).sleepFor(DEFAULT_DELAY_MILLIS);
    }

}
