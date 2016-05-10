package uk.gov.justice.services.adapter.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.adapters.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.jayway.jsonassert.JsonAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestProcessorProducerTest {

    private static final String ID_VALUE = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME_VALUE = "test.command.do-something";
    private static final String ARRAY_ITEM_1 = "Array Item 1";
    private static final String ARRAY_ITEM_2 = "Array Item 2";
    private static final HashMap<String, String> NOT_USED_PATH_PARAMS = new HashMap<>();
    private static final String FIELD_NAME = "name";
    private static final String FIELD_VALUE = "TEST NAME";

    @Mock
    private InjectionPoint queryApiInjectionPoint;

    @Mock
    private InjectionPoint queryControllerInjectionPoint;

    @Mock
    private Member queryApiMember;

    @Mock
    private Member queryControllerMember;

    @Mock
    private Function<JsonEnvelope, JsonEnvelope> function;

    @Mock
    private HttpHeaders httpHeaders;

    @InjectMocks
    private RestProcessorProducer restProcessorProducer;

    @Before
    public void setup() {

        when(queryApiInjectionPoint.getMember()).thenReturn(queryApiMember);
        when(queryControllerInjectionPoint.getMember()).thenReturn(queryControllerMember);

        doReturn(QueryApiAdapter.class).when(queryApiMember).getDeclaringClass();
        doReturn(QueryControllerAdapter.class).when(queryControllerMember).getDeclaringClass();

        restProcessorProducer.envelopeBuilderFactory = new RestEnvelopeBuilderFactory();
        restProcessorProducer.jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        restProcessorProducer.initialise();
    }

    @Test
    public void shouldReturnPayloadOnlyRestProcessorForJsonObject() {
        when(function.apply(any())).thenReturn(envelopeWithJsonObjectPayload());

        Response response = restProcessorProducer.produceRestProcessor(queryApiInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(response, notNullValue());
        assertThat(response.getHeaderString(HeaderConstants.ID), equalTo(ID_VALUE));
        JsonAssert.with(response.getEntity().toString())
                .assertNotDefined(METADATA)
                .assertThat("$." + FIELD_NAME, equalTo(FIELD_VALUE));
    }

    @Test
    public void shouldReturnDefaultRestProcessor() {
        when(function.apply(any())).thenReturn(envelopeWithJsonObjectPayload());

        Response response = restProcessorProducer.produceRestProcessor(queryControllerInjectionPoint)
                .processSynchronously(function, "somecontext.somequery", headersWith("Accept", "application/vnd.somecontext.query.somequery+json"), NOT_USED_PATH_PARAMS);

        assertThat(response, notNullValue());
        JsonAssert.with(response.getEntity().toString())
                .assertThat("$." + FIELD_NAME, equalTo(FIELD_VALUE))
                .assertThat("$." + METADATA + "." + ID, equalTo(ID_VALUE))
                .assertThat("$." + METADATA + "." + NAME, equalTo(NAME_VALUE));
    }

    private JsonEnvelope envelopeWithJsonObjectPayload() {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(FIELD_NAME, FIELD_VALUE);

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadata()), jsonObjectBuilder.build());
    }

    private JsonObject metadata() {
        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
        metadataBuilder.add(ID, ID_VALUE);
        metadataBuilder.add(NAME, NAME_VALUE);

        return metadataBuilder.build();
    }

    @Adapter(QUERY_API)
    private class QueryApiAdapter {

    }

    @Adapter(QUERY_CONTROLLER)
    private class QueryControllerAdapter {

    }

}
