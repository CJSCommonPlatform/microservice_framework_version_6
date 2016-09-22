package uk.gov.justice.services.test.utils.core.http;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.http.PollingRequestParamsBuilder.*;

import java.util.function.Predicate;

@RunWith(MockitoJUnitRunner.class)
public class ResponseValidatorTest {

    @InjectMocks
    private ResponseValidator responseValidator;

    @Test
    public void shouldReturnTrueIfTheExpectedStatusMatchesAndTheConditionSucceeds() throws Exception {

        final Integer status = 200;
        final String responseBody = "some json";
        final Predicate<String> successfulResponseBodyCondition = json -> true;

        final PollingRequestParams pollingRequestParams = pollingRequestParams("a url", "a media type")
                .withResponseBodyCondition(successfulResponseBodyCondition)
                .withExpectedResponseStatus(status)
                .build();


        assertThat(responseValidator.isValid(responseBody, status, pollingRequestParams), is(true));
    }

    @Test
    public void shouldReturnFalseIfTheStatusDoesNotMatch() throws Exception {

        final Integer expectedStatus = 200;
        final Integer actualStatus = 404;
        final String responseBody = "some json";
        final Predicate<String> successfulResponseBodyCondition = json -> true;

        final PollingRequestParams pollingRequestParams = pollingRequestParams("a url", "a media type")
                .withResponseBodyCondition(successfulResponseBodyCondition)
                .withExpectedResponseStatus(expectedStatus)
                .build();


        assertThat(responseValidator.isValid(responseBody, actualStatus, pollingRequestParams), is(false));
    }

    @Test
    public void shouldReturnFalseIfTheResponseBodyConditionFailsButTheStatusMatches() throws Exception {

        final Integer status = 200;
        final String responseBody = "some json";
        final Predicate<String> successfulResponseBodyCondition = json -> false;

        final PollingRequestParams pollingRequestParams = pollingRequestParams("a url", "a media type")
                .withResponseBodyCondition(successfulResponseBodyCondition)
                .withExpectedResponseStatus(status)
                .build();


        assertThat(responseValidator.isValid(responseBody, status, pollingRequestParams), is(false));
    }

    @Test
    public void shouldReturnFalseIfTheExpectedStatusDoesNotMatchAndTheRespnseBodyConditionFails() throws Exception {

        final Integer expectedStatus = 200;
        final Integer actualStatus = 404;
        final String responseBody = "some json";
        final Predicate<String> successfulResponseBodyCondition = json -> false;

        final PollingRequestParams pollingRequestParams = pollingRequestParams("a url", "a media type")
                .withResponseBodyCondition(successfulResponseBodyCondition)
                .withExpectedResponseStatus(expectedStatus)
                .build();


        assertThat(responseValidator.isValid(responseBody, actualStatus, pollingRequestParams), is(false));
    }
}
