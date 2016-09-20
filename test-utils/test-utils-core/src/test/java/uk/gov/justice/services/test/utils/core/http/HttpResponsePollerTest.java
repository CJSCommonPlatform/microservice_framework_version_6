package uk.gov.justice.services.test.utils.core.http;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpResponsePollerTest {

    private final static String EXPECTED_MESSAGE_404 = "Failed to match response conditions from http://localhost:8080/poll/condition, after 5 attempts, with status code: 404";
    private final static String EXPECTED_MESSAGE_200 = "Failed to match response conditions from http://localhost:8080/poll/condition, after 5 attempts, with status code: 200";
    private final static String EXPECTED_MESSAGE_RESULT = "Failed to match result conditions from http://localhost:8080/poll/condition, after 5 attempts, with result: ";

    private final String URL = "http://localhost:8080/poll/condition";
    private final String MEDIA_TYPE = "application/vnd.poll.condition+json";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RestClient restClient;

    @Mock
    private Response response;

    @InjectMocks
    private HttpResponsePoller poller;

    @Test
    public void shouldReturnResultEntityIfFound() {

        final String responseText = "RESPONSE";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        final String result = poller.pollUntilFound(URL, MEDIA_TYPE);

        assertThat(result, is(responseText));
    }

    @Test
    public void shouldFailPollOnFoundWithNotFoundResponse() {

        final String responseText = "RESPONSE";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(NOT_FOUND.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_404);

        final String result = poller.pollUntilFound(URL, MEDIA_TYPE);

        assertThat(result, is(responseText));
    }

    @Test
    public void shouldReturnNullValueIfNotFound() {

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(NOT_FOUND.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(null);

        final String result = poller.pollUntilNotFound(URL, MEDIA_TYPE);

        assertThat(result, nullValue());
    }

    @Test
    public void shouldPollAndFailIfFound() {

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

        final String result = poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, responseText::equals);

        assertThat(result, is(responseText));
    }

    @Test
    public void shouldPollAndFailIfNullResponseEntity() {

        final String responseConditionMet = "Condition Met";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(null);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_RESULT);

        poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, responseConditionMet::equals);
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

        poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, responseConditionMet::equals);
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

        final String result = poller.pollUntilFoundWithCondition(URL, MEDIA_TYPE, responseConditionMet::equals);

        assertThat(result, is(responseConditionMet));
    }

    @Test
    public void shouldPollUntilJsonObjectWithMatchingValuesIsFound() throws Exception {

        final String matchingJsonObject = "{\"name\": \"test\",\"email\": \"test@email.co.uk\"}";
        final String unMatchingJsonObject = "{\"name\": \"no match\"}";

        final Map<String, String> matchValues = new HashMap<>();
        matchValues.put("name", "test");
        matchValues.put("email", "test@email.co.uk");

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());

        when(response.readEntity(String.class))
                .thenReturn(unMatchingJsonObject)
                .thenReturn(matchingJsonObject);

        final String result = poller.pollUntilJsonObjectFoundWithValues(URL, MEDIA_TYPE, matchValues);

        assertThat(result, is(matchingJsonObject));
    }

    @Test
    public void shouldFailIfValueIsNotFound() throws Exception {

        final String unMatchingJsonObject = "{\"name\": \"no match\"}";
        final Map<String, String> matchValues = singletonMap("name", "test");

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());

        when(response.readEntity(String.class))
                .thenReturn(unMatchingJsonObject);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_RESULT);

        poller.pollUntilJsonObjectFoundWithValues(URL, MEDIA_TYPE, matchValues);
    }

    @Test
    public void shouldFailIfASingleValueOfMultipleValuesIsNotFound() throws Exception {

        final String matchingJsonObject = "{\"name\": \"test\",\"email\": \"test@email.co.uk\"}";
        final Map<String, String> matchValues = new HashMap<>();
        matchValues.put("name", "test");
        matchValues.put("email", "notmatch@email.co.uk");

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());

        when(response.readEntity(String.class))
                .thenReturn(matchingJsonObject);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage(EXPECTED_MESSAGE_RESULT);

        poller.pollUntilJsonObjectFoundWithValues(URL, MEDIA_TYPE, matchValues);
    }

    @Test
    public void shouldPassHeadersToRestClientIfItsProvided() {

        final HttpResponsePoller testObj = new HttpResponsePoller(restClient);

        final String responseText = "Condition Met";

        MultivaluedMap<String, Object> arbitraryHerader = new MultivaluedMapImpl<>();
        when(restClient.query(URL, MEDIA_TYPE, arbitraryHerader)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        final String result = testObj.withHeaders(arbitraryHerader).pollUntilFoundWithCondition(URL, MEDIA_TYPE, responseText::equals);

        assertThat(result, is(responseText));
    }

    @Test
    public void shouldIgnoreHeadersIfItsNotProvided() {

        final HttpResponsePoller testObj = new HttpResponsePoller(restClient);

        final String responseText = "Condition Met";

        when(restClient.query(URL, MEDIA_TYPE)).thenReturn(response);
        when(response.getStatus()).thenReturn(OK.getStatusCode());
        when(response.readEntity(String.class)).thenReturn(responseText);

        final String result = testObj.withHeaders(null).pollUntilFoundWithCondition(URL, MEDIA_TYPE, responseText::equals);

        assertThat(result, is(responseText));
    }

}
