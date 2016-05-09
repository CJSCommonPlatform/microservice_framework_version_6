package uk.gov.justice.services.clients.core;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static javax.json.JsonValue.NULL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestClientProcessorIT {

    private static final String RESPONSE_STRING = "Test Response";

    private static final String BASE_URI = "http://localhost:8089";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Mock
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Mock
    private JsonObject responseAsJsonObject;

    @Mock
    private Metadata metadata;

    @Mock
    private JsonEnvelope requestEnvelope;

    @Mock
    private JsonEnvelope responseEnvelope;

    @InjectMocks
    private RestClientProcessor restClientProcessor;

    @Before
    public void setup() {
        when(requestEnvelope.metadata()).thenReturn(metadata);
        when(stringToJsonObjectConverter.convert(RESPONSE_STRING)).thenReturn(responseAsJsonObject);
        when(jsonObjectEnvelopeConverter.asEnvelope(responseAsJsonObject)).thenReturn(responseEnvelope);
    }

    @Test
    public void shouldCallRestServiceWithNoParameters() throws Exception {

        final String path = "/my/resource";
        final String name = "context.query.myquery";
        final String mimetype = format("application/vnd.%s+json", name);

        stubFor(get(urlEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withBody(RESPONSE_STRING)));

        when(metadata.name()).thenReturn(name);

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), emptySet());

        JsonEnvelope response = restClientProcessor.request(endpointDefinition, requestEnvelope);

        assertThat(response, equalTo(responseEnvelope));
    }

    @Test
    public void shouldCallRestServiceWithPathParameters() throws Exception {

        final String path = "/my/resource/{paramA}/{paramB}";
        final String name = "context.query.myquery";
        final String mimetype = format("application/vnd.%s+json", name);

        stubFor(get(urlEqualTo("/my/resource/valueA/valueB"))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withBody(RESPONSE_STRING)));

        JsonObject payload = Json.createObjectBuilder()
                .add("paramA", "valueA")
                .add("paramB", "valueB")
                .build();
        when(metadata.name()).thenReturn(name);
        when(requestEnvelope.payloadAsJsonObject()).thenReturn(payload);

        Set<String> pathParams = ImmutableSet.of("paramA", "paramB");

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, pathParams, emptySet());

        JsonEnvelope response = restClientProcessor.request(endpointDefinition, requestEnvelope);

        assertThat(response, equalTo(responseEnvelope));
    }

    @Test
    public void shouldCallRestServiceWithQueryParameters() throws Exception {

        final String path = "/my/resource";
        final String name = "context.query.myquery";
        final String mimetype = format("application/vnd.%s+json", name);

        stubFor(get(urlPathEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .withQueryParam("paramA", WireMock.equalTo("valueA"))
                .withQueryParam("paramC", WireMock.equalTo("valueC"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withBody(RESPONSE_STRING)));

        JsonObject payload = Json.createObjectBuilder()
                .add("paramA", "valueA")
                .add("paramC", "valueC")
                .build();
        when(metadata.name()).thenReturn(name);
        when(requestEnvelope.payloadAsJsonObject()).thenReturn(payload);

        Set<QueryParam> queryParams = ImmutableSet.of(new QueryParam("paramA", true), new QueryParam("paramB", false), new QueryParam("paramC", true));

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), queryParams);

        JsonEnvelope response = restClientProcessor.request(endpointDefinition, requestEnvelope);

        assertThat(response, equalTo(responseEnvelope));
    }

    @Test
    public void shouldReturnJsonNullPayloadFor404ResponseCode() throws Exception {

        final String path = "/my/resource";
        final String name = "context.query.myquery";
        final String mimetype = format("application/vnd.%s+json", name);

        stubFor(get(urlEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", mimetype)
                        .withBody(RESPONSE_STRING)));

        restClientProcessor.enveloper = new Enveloper();

        when(requestEnvelope.metadata()).thenReturn(metadata());
        when(responseEnvelope.payload()).thenReturn(JsonValue.NULL);

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), emptySet());

        JsonEnvelope response = restClientProcessor.request(endpointDefinition, requestEnvelope);

        assertThat(response, notNullValue());
        assertThat(response.payload(), equalTo(NULL));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionFor500ResponseCode() throws Exception {

        final String path = "/my/resource";
        final String name = "context.query.myquery";
        final String mimetype = format("application/vnd.%s+json", name);

        stubFor(get(urlEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", mimetype)
                        .withBody(RESPONSE_STRING)));

        when(metadata.name()).thenReturn(name);

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), emptySet());

        restClientProcessor.request(endpointDefinition, requestEnvelope);
    }

    private Metadata metadata() {
        return JsonObjectMetadata.metadataFrom(Json.createObjectBuilder()
                .add("name", "test.query.get-something")
                .add("id", UUID.randomUUID().toString()).build());
    }

}
