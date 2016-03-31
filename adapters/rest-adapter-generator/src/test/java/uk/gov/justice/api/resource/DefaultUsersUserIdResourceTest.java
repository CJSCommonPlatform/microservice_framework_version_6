package uk.gov.justice.api.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.messaging.Envelope;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Consumer;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultEnvelope.envelopeFrom;

/**
 * Unit tests for the generated {@link DefaultUsersUserIdResource} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultUsersUserIdResourceTest {
    private static final String NOT_USED_USER_ID = "1";
    private static final JsonObject NOT_USED_ENTITY = createObjectBuilder().build();

    @Mock
    private AsynchronousDispatcher dispatcher;

    @Mock
    private RestProcessor restProcessor;

    @InjectMocks
    private UsersUserIdResource resource = new DefaultUsersUserIdResource();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessorWhenCreatingUser() throws Exception {

        Response processorResponse = Response.ok().build();
        when(restProcessor.process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class))).thenReturn(processorResponse);

        Response resourceResponse = resource.postVndPeopleCommandsCreateUserJsonUsersByUserId(NOT_USED_USER_ID, NOT_USED_ENTITY);
        assertThat(resourceResponse, is(processorResponse));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessorWhenUpdatingUser() throws Exception {

        Response processorResponse = Response.ok().build();
        when(restProcessor.process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class))).thenReturn(processorResponse);

        Response resourceResponse = resource.postVndPeopleCommandsUpdateUserJsonUsersByUserId(NOT_USED_USER_ID, NOT_USED_ENTITY);
        assertThat(resourceResponse, is(processorResponse));

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldCallDispatcherWhenCreatingUser() throws Exception {

        resource.postVndPeopleCommandsCreateUserJsonUsersByUserId(NOT_USED_USER_ID, NOT_USED_ENTITY);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(restProcessor).process(consumerCaptor.capture(), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class));

        Envelope envelope = envelopeFrom(null, null);
        consumerCaptor.getValue().accept(envelope);

        verify(dispatcher).dispatch(envelope);

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldCallDispatcherWhenUpdatingUser() throws Exception {

        resource.postVndPeopleCommandsUpdateUserJsonUsersByUserId(NOT_USED_USER_ID, NOT_USED_ENTITY);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(restProcessor).process(consumerCaptor.capture(), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class));

        Envelope envelope = envelopeFrom(null, null);
        consumerCaptor.getValue().accept(envelope);

        verify(dispatcher).dispatch(envelope);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassEntityToRestProcessorWhenCreatingUser() throws Exception {

        JsonObject entity = createObjectBuilder().add("paramNameA", "valueABCD").build();

        resource.postVndPeopleCommandsUpdateUserJsonUsersByUserId(NOT_USED_USER_ID, entity);

        verify(restProcessor).process(any(Consumer.class), eq(entity), any(HttpHeaders.class), any(Map.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassEntityToRestProcessorWhenUpdatingUser() throws Exception {

        JsonObject entity = createObjectBuilder().add("paramNameB", "valueEFG").build();

        resource.postVndPeopleCommandsUpdateUserJsonUsersByUserId(NOT_USED_USER_ID, entity);

        verify(restProcessor).process(any(Consumer.class), eq(entity), any(HttpHeaders.class), any(Map.class));

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldPassUserIdToRestProcessorWhenCreatingUser() throws Exception {

        resource.postVndPeopleCommandsUpdateUserJsonUsersByUserId("user1234", NOT_USED_ENTITY);
        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(1));
        assertThat(pathParams.containsKey("userId"), is(true));
        assertThat(pathParams.get("userId"), is("user1234"));


    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldPassUserIdToRestProcessorWhenUpdatingUser() throws Exception {

        resource.postVndPeopleCommandsUpdateUserJsonUsersByUserId("user5678", NOT_USED_ENTITY);
        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).process(any(Consumer.class), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(1));
        assertThat(pathParams.containsKey("userId"), is(true));
        assertThat(pathParams.get("userId"), is("user5678"));

    }

}
