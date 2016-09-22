package uk.gov.justice.services.test.utils.core.http;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParams.DEFAULT_DELAY_MILLIS;

import uk.gov.justice.services.test.utils.core.helper.Sleeper;

import java.util.function.Predicate;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class PollingRestClientTest {

    @Mock
    private ValidatingRestClient validatingRestClient;

    @Mock
    private Sleeper sleeper;

    @Mock
    private ResponseValidator responseValidator;

    @InjectMocks
    private PollingRestClient pollingRestClient;

    @Test @SuppressWarnings("unchecked")
    public void shouldPollUntilASucessfulResponseIsFound() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final Status status = OK;
        final String responseBody = "{\"some\": \"json\"}";

        final Response response = mock(Response.class);
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);
        final ResponseDetails responseDetails = new ResponseDetails(status, responseBody);

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withExpectedResponseStatus(status)
                .withResponseBodyCondition(alwaysTrueResultCondition)
                .build();

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), of(responseDetails));
        when(response.getStatus()).thenReturn(status.getStatusCode());
        when(response.getEntity()).thenReturn(responseBody);
        when(alwaysTrueResultCondition.test(responseBody)).thenReturn(true);
        when(responseValidator.hasValidResponseBody(responseBody, pollingRequestParams)).thenReturn(true);
        when(responseValidator.hasValidStatus(status, pollingRequestParams)).thenReturn(true);
        assertThat(pollingRestClient.pollUntilExpectedResponse(pollingRequestParams), is(responseBody));

        verify(sleeper, times(2)).sleepFor(DEFAULT_DELAY_MILLIS);
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldThrowAssertionErrorWhenResponseBodyIsNotValid() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final Status status = OK;
        final String responseBody = "{\"some\": \"json\"}";

        final Response response = mock(Response.class);
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);
        final ResponseDetails responseDetails = new ResponseDetails(status, responseBody);

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withExpectedResponseStatus(status)
                .withResponseBodyCondition(alwaysTrueResultCondition)
                .build();

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), of(responseDetails));
        when(response.getStatus()).thenReturn(status.getStatusCode());
        when(response.getEntity()).thenReturn(responseBody);
        when(alwaysTrueResultCondition.test(responseBody)).thenReturn(true);
        when(responseValidator.hasValidResponseBody(responseBody, pollingRequestParams)).thenReturn(false);
        when(responseValidator.hasValidStatus(status, pollingRequestParams)).thenReturn(true);

        try {
            pollingRestClient.pollUntilExpectedResponse(pollingRequestParams);
        } catch (AssertionError expected) {
            assertThat(expected.getMessage(), is("Failed to match result conditions from http://url.com, after 5 attempts, with result: " + responseBody));
        }
    }

    @Test @SuppressWarnings("unchecked")
    public void shouldThrowAssertionErrorWhenResponseStatusIsNotValid() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final Status expectedStatus = OK;
        final Status actualStatus = NOT_FOUND;
        final String responseBody = "{\"some\": \"json\"}";

        final Response response = mock(Response.class);
        final Predicate<String>  alwaysTrueResultCondition = mock(Predicate.class);
        final ResponseDetails responseDetails = new ResponseDetails(actualStatus, responseBody);

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withExpectedResponseStatus(expectedStatus)
                .withResponseBodyCondition(alwaysTrueResultCondition)
                .build();

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), of(responseDetails));
        when(response.getStatus()).thenReturn(actualStatus.getStatusCode());
        when(response.getEntity()).thenReturn(responseBody);
        when(alwaysTrueResultCondition.test(responseBody)).thenReturn(true);
        when(responseValidator.hasValidResponseBody(responseBody, pollingRequestParams)).thenReturn(true);
        when(responseValidator.hasValidStatus(actualStatus, pollingRequestParams)).thenReturn(false);

        try {
            pollingRestClient.pollUntilExpectedResponse(pollingRequestParams);
        } catch (AssertionError expected) {
            assertThat(expected.getMessage(), is("Incorrect http response status received from http://url.com. Expected OK, received Not Found"));
        }
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
    public void shouldThrowAnAssertionExceptionIfTheResultConditionFails() throws Exception {

        final String url = "http://url.com";
        final String mediaType = "application/vnd.media.type+json";
        final Status status = OK;
        final String responseBody = "{\"some\": \"json\"}";

        final Response response = mock(Response.class);
        final Predicate<Response>  alwaysTrueResponseCondition = mock(Predicate.class);
        final Predicate<String>  alwaysFalseResultCondition = mock(Predicate.class);

        final PollingRequestParams pollingRequestParams = new PollingRequestParamsBuilder(url, mediaType)
                .withResponseBodyCondition(alwaysFalseResultCondition)
                .build();
        final ResponseDetails responseDetails = new ResponseDetails(status, responseBody);

        when(validatingRestClient.get(pollingRequestParams)).thenReturn(empty(), empty(), of(responseDetails));
        when(response.getStatus()).thenReturn(status.getStatusCode());
        when(response.getEntity()).thenReturn(responseBody);
        when(alwaysTrueResponseCondition.test(response)).thenReturn(true);
        when(alwaysFalseResultCondition.test(responseBody)).thenReturn(false);

        try {
            pollingRestClient.pollUntilExpectedResponse(pollingRequestParams);
        } catch (AssertionError expected) {
            assertThat(expected.getMessage(), is("Failed to match result conditions from http://url.com, after 5 attempts, with result: " + responseBody));
        }

        verify(sleeper, times(2)).sleepFor(DEFAULT_DELAY_MILLIS);
    }

}
