package uk.gov.justice.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.DispatcherProducer;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.io.StringWriter;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@ServiceComponent(QUERY_API)
public class RemoteExampleQueryApiIT {

    private static final String BASE_PATH = "/rest-adapter-generator/query/api/rest/example";
    private static final String METADATA = "_metadata";
    private static final JsonObject RESPONSE = Json.createObjectBuilder()
            .add(METADATA, Json.createObjectBuilder()
                    .add(NAME, "people.response.user")
                    .add(ID, UUID.randomUUID().toString()))
            .add("result", "SUCCESS")
            .build();
    private static final UUID QUERY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static int port = -1;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);
    @Inject
    Requester requester;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    private static String jsonObjectToString(final JsonObject source) {
        final StringWriter stringWriter = new StringWriter();
        try (final JsonWriter writer = Json.createWriter(stringWriter)) {
            writer.writeObject(source);
        }

        return stringWriter.getBuffer().toString();
    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Module
    @Classes(cdi = true, value = {
            RemoteExampleQueryApi.class,
            RestClientProcessor.class,
            RestClientHelper.class,
            DispatcherProducer.class,
            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapper.class,
            Enveloper.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("rest-client-generator")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldSendQueryRemoteServiceAndReturnResponse() {

        final String name = "people.query.get-user";
        final JsonObject metadata = Json.createObjectBuilder()
                .add(NAME, name)
                .add(ID, QUERY_ID.toString())
                .build();
        final JsonObject payload = Json.createObjectBuilder()
                .add("userId", USER_ID.toString())
                .build();

        final JsonEnvelope query = envelopeFrom(metadataFrom(metadata), payload);

        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = format("application/vnd.%s+json", name);

        stubFor(get(urlEqualTo(BASE_PATH + path))
                .withHeader("Accept", WireMock.equalTo(mimeType))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimeType)
                        .withBody(jsonObjectToString(RESPONSE))));

        JsonEnvelope response = requester.request(query);
        assertThat(response.payloadAsJsonObject(), equalTo(
                Json.createObjectBuilder()
                        .add("result", "SUCCESS")
                        .build()));
    }
}
