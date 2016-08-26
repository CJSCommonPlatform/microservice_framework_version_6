package uk.gov.justice.services.test.utils.core.http;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.test.utils.core.rest.RestClient;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpResponsePollerTest {

    private final static String EXPECTED_MESSAGE_404 = "Failed to get 404 response from 'http://localhost:8080/poll/condition' after 5 attempts. Status code: 404";
    private final static String EXPECTED_MESSAGE_200 = "Failed to get 200 response from 'http://localhost:8080/poll/condition' after 5 attempts. Status code: 200";
    private final String URL = "http://localhost:8080/poll/condition";
    private final String MEDIA_TYPE = "application/vnd.poll.condition+json";
    private final int DELAY_IN_MILLIS = 100;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private RestClient restClient;
    @Mock
    private Response response;

    private HttpResponsePoller poller;

    @Before
    public void setup() throws Exception {
        poller = new HttpResponsePoller(restClient);
    }

    @Test
    public void shouldPollOnFoundResponseUntilSuccessful() {

        final String responseText = "RESPONSE";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        poller.pollUntilFound(URL, MEDIA_TYPE);
    }

    @Test
    public void shouldFailPollOnFoundWithNotFoundResponse() {

        final String responseText = "RESPONSE";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(NOT_FOUND.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_404);

        poller.pollUntilFound(URL, MEDIA_TYPE);
    }

    @Test
    public void shouldPollOnNotFoundResponseUntilSuccessful() {

        final String responseText = "RESPONSE";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(NOT_FOUND.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        poller.pollUntilNotFound(URL, MEDIA_TYPE);
    }

    @Test
    public void shouldFailPollOnNotFoundWithFoundResponse() {

        final String responseText = "RESPONSE";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_200);

        poller.pollUntilNotFound(URL, MEDIA_TYPE);
    }

    @Test
    public void shouldPollOnConditionUntilSuccessful() {

        final String responseText = "Condition Met";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, DELAY_IN_MILLIS, response -> response.equals(responseText));
    }

    @Test
    public void shouldPollAndFailOnNullResponseOnCondition() {

        final String responseConditionMet = "Condition Met";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(null);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_200);

        poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, DELAY_IN_MILLIS, response -> response.equals(responseConditionMet));
    }


    @Test
    public void shouldPollAndFailWhenConditionNotMetAfter5Retries() {

        final String responseConditionMet = "Condition Met";
        final String responseConditionNotMet = "Condition not Met";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(NOT_FOUND.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseConditionNotMet);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_404);

        poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, DELAY_IN_MILLIS, response -> response.equals(responseConditionMet));
    }

    @Test
    public void shouldPollAndRespondWhenConditionMetAfter3Retries() {

        final String responseConditionMet = "Condition Met";
        final String responseConditionNotMet = "Condition not Met";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());

        when(response.readEntity(String.class))
                .thenReturn(responseConditionNotMet)
                .thenReturn(responseConditionNotMet)
                .thenReturn(responseConditionMet);

        poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, DELAY_IN_MILLIS, response -> response.equals(responseConditionMet));
    }

}
