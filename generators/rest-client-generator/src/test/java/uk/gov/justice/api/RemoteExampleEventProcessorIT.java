package uk.gov.justice.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcherProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcherProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.jms.JmsDestinations;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;

import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@ServiceComponent(EVENT_PROCESSOR)
public class RemoteExampleEventProcessorIT {

    private static final String BASE_PATH = "/rest-client-generator/command/api/rest/example";
    private static final UUID QUERY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_NAME = "John Smith";

    private static int port = -1;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Inject
    Sender sender;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
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
            RemoteExampleCommandApi.class,
            RestClientProcessor.class,
            RestClientHelper.class,
            DispatcherCache.class,
            AsynchronousDispatcherProducer.class,
            SynchronousDispatcherProducer.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,

            // TODO: Remove the next 6 classes when sender is migrated fully to dispatcher system
            SenderProducer.class,
            JmsSenderFactory.class,
            ComponentDestination.class,
            DefaultJmsEnvelopeSender.class,
            JmsDestinations.class,
            EnvelopeConverter.class,

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
    public void shouldSendCommandToRemoteService() {

        final String name = "people.command.update-user";
        final JsonObject metadata = createObjectBuilder()
                .add(NAME, name)
                .add(ID, QUERY_ID.toString())
                .build();
        final JsonObject payload = createObjectBuilder()
                .add("userId", USER_ID.toString())
                .add("userName", USER_NAME)
                .build();
        final String bodyPayload = createObjectBuilder().add("userName", USER_NAME).build().toString();

        final JsonEnvelope command = envelopeFrom(metadataFrom(metadata), payload);

        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = format("application/vnd.%s+json", name);

        stubFor(post(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(command);

        verify(postRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
                .withRequestBody(equalToJson(bodyPayload))
        );
    }
}
