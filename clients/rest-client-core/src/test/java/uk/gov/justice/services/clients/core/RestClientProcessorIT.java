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

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(MockitoJUnitRunner.class)
public class RestClientProcessorIT {

    private static final String REQUEST_PARAM_A_PARAM_B_FILE_NAME = "request-envelope-a-b";
    private static final String REQUEST_PARAM_A_PARAM_C_FILE_NAME = "request-envelope-a-c";
    private static final String RESPONSE_WITh_METADATA_FILE_NAME = "response-with-metadata";
    private static final String RESPONSE_WITHOUT_METADATA_FILE_NAME = "response-without-metadata";

    private static final String BASE_URI = "http://localhost:8089";
    private static final String CONTEXT_QUERY_MY_QUERY = "context.query.myquery";
    private static final String PAYLOAD_ID_NAME = "payloadId";
    private static final String PAYLOAD_ID_VALUE = "c3f7182b-bd20-4678-ba8b-e7e5ea8629c3";
    private static final String PAYLOAD_VERSION_NAME = "payloadVersion";
    private static final int PAYLOAD_VERSION_VALUE = 0;
    private static final String PAYLOAD_NAME_NAME = "payloadName";
    private static final String PAYLOAD_NAME_VALUE = "Name of the Payload";
    private static final String METADATA_ID_VALUE = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String METADATA_ID = "CPPID";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    private RestClientProcessor restClientProcessor;

    private String envelopeWithMetadataAsJson;
    private String envelopeWithoutMetadataAsJson;

    @Before
    public void setup() throws IOException {
        restClientProcessor = new RestClientProcessor();
        restClientProcessor.stringToJsonObjectConverter = new StringToJsonObjectConverter();
        restClientProcessor.jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        restClientProcessor.enveloper = new Enveloper();
        envelopeWithMetadataAsJson = responseWithMetadata();
        envelopeWithoutMetadataAsJson = jsonFromFile(RESPONSE_WITHOUT_METADATA_FILE_NAME);
    }

    @Test
    public void shouldCallRestServiceWithNoParameters() throws Exception {

        final String path = "/my/resource";
        final String mimetype = format("application/vnd.%s+json", CONTEXT_QUERY_MY_QUERY);

        stubFor(get(urlEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withBody(responseWithMetadata())));

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), emptySet());

        validateResponse(restClientProcessor.request(endpointDefinition, requestEnvelopeParamAParamB()), envelopeWithMetadataAsJson);
    }


    @Test
    public void shouldCallRestServiceWithPathParameters() throws Exception {

        final String path = "/my/resource/{paramA}/{paramB}";
        final String mimetype = format("application/vnd.%s+json", CONTEXT_QUERY_MY_QUERY);

        stubFor(get(urlEqualTo("/my/resource/valueA/valueB"))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withBody(responseWithMetadata())));

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, ImmutableSet.of("paramA", "paramB"), emptySet());

        validateResponse(restClientProcessor.request(endpointDefinition, requestEnvelopeParamAParamB()), envelopeWithMetadataAsJson);
    }

    @Test
    public void shouldHandleRemoteResponseWithMetadata() throws Exception {

        final String path = "/my/resource";
        final String mimetype = format("application/vnd.%s+json", CONTEXT_QUERY_MY_QUERY);

        stubFor(get(urlPathEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .withQueryParam("paramA", WireMock.equalTo("valueA"))
                .withQueryParam("paramC", WireMock.equalTo("valueC"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withBody(responseWithMetadata())));

        Set<QueryParam> queryParams = ImmutableSet.of(new QueryParam("paramA", true), new QueryParam("paramB", false), new QueryParam("paramC", true));

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), queryParams);

        validateResponse(restClientProcessor.request(endpointDefinition, requestEnvelopeParamAParamC()), envelopeWithMetadataAsJson);
    }

    @Test
    public void shouldHandleRemoteResponseWithoutMetadata() throws Exception {

        final String path = "/my/resource";
        final String mimetype = format("application/vnd.%s+json", CONTEXT_QUERY_MY_QUERY);

        stubFor(get(urlPathEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .withQueryParam("paramA", WireMock.equalTo("valueA"))
                .withQueryParam("paramC", WireMock.equalTo("valueC"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withHeader(METADATA_ID, METADATA_ID_VALUE)
                        .withBody(envelopeWithoutMetadataAsJson)));

        Set<QueryParam> queryParams = ImmutableSet.of(new QueryParam("paramA", true), new QueryParam("paramB", false), new QueryParam("paramC", true));

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), queryParams);

        validateResponse(restClientProcessor.request(endpointDefinition, requestEnvelopeParamAParamC()), envelopeWithMetadataAsJson);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnQueryParamMissing() throws Exception {

        final String path = "/my/resource";
        final String mimetype = format("application/vnd.%s+json", CONTEXT_QUERY_MY_QUERY);

        stubFor(get(urlPathEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .withQueryParam("paramA", WireMock.equalTo("valueA"))
                .withQueryParam("paramC", WireMock.equalTo("valueC"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimetype)
                        .withBody(responseWithMetadata())));

        Set<QueryParam> queryParams = ImmutableSet.of(new QueryParam("paramA", true), new QueryParam("paramC", true));

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), queryParams);

        restClientProcessor.request(endpointDefinition, requestEnvelopeParamAParamB());
    }

    @Test
    public void shouldReturnJsonNullPayloadFor404ResponseCode() throws Exception {

        final String path = "/my/resource";
        final String mimetype = format("application/vnd.%s+json", CONTEXT_QUERY_MY_QUERY);

        stubFor(get(urlEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", mimetype)
                        .withBody(responseWithMetadata())));

        restClientProcessor.enveloper = new Enveloper();

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), emptySet());

        JsonEnvelope response = restClientProcessor.request(endpointDefinition, requestEnvelopeParamAParamB());

        assertThat(response, notNullValue());
        assertThat(response.payload(), equalTo(NULL));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionFor500ResponseCode() throws Exception {

        final String path = "/my/resource";
        final String mimetype = format("application/vnd.%s+json", CONTEXT_QUERY_MY_QUERY);

        stubFor(get(urlEqualTo(path))
                .withHeader("Accept", WireMock.equalTo(mimetype))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", mimetype)
                        .withBody(responseWithMetadata())));

        EndpointDefinition endpointDefinition = new EndpointDefinition(BASE_URI, path, emptySet(), emptySet());

        restClientProcessor.request(endpointDefinition, requestEnvelopeParamAParamB());
    }

    private String jsonFromFile(String jsonFileName) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", jsonFileName)), Charset.defaultCharset());
    }

    private JsonEnvelope requestEnvelopeParamAParamB() throws IOException {
        return new JsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter().convert(jsonFromFile(REQUEST_PARAM_A_PARAM_B_FILE_NAME)));
    }

    private JsonEnvelope requestEnvelopeParamAParamC() throws IOException {
        return new JsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter().convert(jsonFromFile(REQUEST_PARAM_A_PARAM_C_FILE_NAME)));
    }

    private String responseWithMetadata() throws IOException {
        return jsonFromFile(RESPONSE_WITh_METADATA_FILE_NAME);
    }

    private void validateResponse(JsonEnvelope response, String expectedResponseJson) {
        assertThat(response.metadata(), notNullValue());
        assertThat(response.metadata().id().toString(), equalTo(METADATA_ID_VALUE));
        assertThat(response.metadata().name(), equalTo(CONTEXT_QUERY_MY_QUERY));

        JSONAssert.assertEquals(expectedResponseJson, new JsonObjectEnvelopeConverter().fromEnvelope(response).toString(), false);

        assertThat(response.payloadAsJsonObject().getString(PAYLOAD_ID_NAME), equalTo(PAYLOAD_ID_VALUE));
        assertThat(response.payloadAsJsonObject().getInt(PAYLOAD_VERSION_NAME), equalTo(PAYLOAD_VERSION_VALUE));
        assertThat(response.payloadAsJsonObject().getString(PAYLOAD_NAME_NAME), equalTo(PAYLOAD_NAME_VALUE));
    }

}
