package uk.gov.justice.services.adapter.rest.processor;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonValue;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ResponseStrategyHelperTest {

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Function<JsonEnvelope, Response> function;

    @Mock
    private Response response;

    @Mock
    private Logger logger;

    @InjectMocks
    private ResponseFactoryHelper responseFactoryHelper;

    @Test
    public void shouldCallApplyOnFunctionOnOkResponse() throws Exception {
        when(function.apply(jsonEnvelope)).thenReturn(response);

        final Response result = responseFactoryHelper.responseFor("action.name", Optional.of(jsonEnvelope), function);

        assertThat(result, sameInstance(response));
    }

    @Test
    public void shouldReturnNotFoundResponse() throws Exception {
        when(jsonEnvelope.payload()).thenReturn(JsonValue.NULL);

        final Response response = responseFactoryHelper.responseFor("action.name", Optional.of(jsonEnvelope), function);

        assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturnInternalServerErrorResponse() throws Exception {
        final Response response = responseFactoryHelper.responseFor("action.name", Optional.empty(), function);

        assertThat(response.getStatus(), equalTo(INTERNAL_SERVER_ERROR.getStatusCode()));
    }
}