package uk.gov.justice.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.services.clients.core.DefaultRestClientHelper;
import uk.gov.justice.services.clients.core.DefaultRestClientProcessor;
import uk.gov.justice.services.clients.core.webclient.BaseUriFactory;
import uk.gov.justice.services.clients.core.webclient.ContextMatcher;
import uk.gov.justice.services.clients.core.webclient.MockServerPortProvider;
import uk.gov.justice.services.clients.core.webclient.WebTargetFactory;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.rest.DefaultServerPortProvider;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.dispatcher.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.jms.DefaultJmsDestinations;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;

import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;

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

    private static int port = -1;
    private static final String BASE_PATH = "/rest-client-generator/command/api/rest/example";
    private static final String MOCK_SERVER_PORT = "mock.server.port";

    private static final UUID QUERY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final String USER_NAME = "John Smith";
    private static final String GROUP_NAME = "admin";

    private static final JsonEnvelope RESPONSE = DefaultJsonEnvelope.envelope()
            .with(JsonObjectMetadata.metadataWithRandomUUID("people.group"))
            .withPayloadOf(GROUP_ID, "groupId")
            .withPayloadOf(GROUP_NAME, "groupName")
            .build();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9090);

    @Inject
    Sender sender;

    @Inject
    Requester requester;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        System.setProperty(MOCK_SERVER_PORT, "9090");
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
            AccessControlFailureMessageGenerator.class,
            DefaultAccessControlService.class,
            AllowAllPolicyEvaluator.class,
            BaseUriFactory.class,
            BeanInstantiater.class,
            ContextMatcher.class,
            DefaultServerPortProvider.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            EmptySystemUserProvider.class,
            DefaultEnveloper.class,
            InterceptorCache.class,
            InterceptorChainProcessor.class,
            InterceptorChainProcessorProducer.class,
            JndiBasedServiceContextNameProvider.class,
            DefaultJsonObjectEnvelopeConverter.class,
            LoggerProducer.class,
            MockServerPortProvider.class,
            ObjectMapperProducer.class,
            ObjectToJsonValueConverter.class,
            PolicyEvaluator.class,
            RemoteExampleCommandApi.class,
            RequesterProducer.class,
            DefaultRestClientHelper.class,
            DefaultRestClientProcessor.class,
            ServiceComponentObserver.class,
            StringToJsonObjectConverter.class,
            SystemUserUtil.class,
            WebTargetFactory.class,
            UtcClock.class,

            // TODO: Remove the next 6 classes when sender is migrated fully to dispatcher system
            SenderProducer.class,
            JmsSenderFactory.class,
            ComponentDestination.class,
            DefaultJmsEnvelopeSender.class,
            DefaultJmsDestinations.class,
            DefaultEnvelopeConverter.class,

            DefaultJsonSchemaValidator.class,
            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,
            DefaultTraceLogger.class,
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("rest-client-generator")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldSendPostCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.create-user+json";
        final String bodyPayload = createObjectBuilder().add("userName", USER_NAME).build().toString();

        stubFor(post(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.create-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(postRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
                .withRequestBody(equalToJson(bodyPayload))
        );
    }

    @Test
    public void shouldSendPutCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.update-user+json";
        final String bodyPayload = createObjectBuilder().add("userName", USER_NAME).build().toString();

        stubFor(put(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.update-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(putRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
                .withRequestBody(equalToJson(bodyPayload))
        );
    }

    @Test
    public void shouldSendPatchCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.modify-user+json";
        final String bodyPayload = createObjectBuilder().add("userName", USER_NAME).build().toString();

        stubFor(patch(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.modify-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(patchRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
                .withRequestBody(equalToJson(bodyPayload))
        );
    }

    @Test
    public void shouldSendDeleteCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.delete-user+json";

        stubFor(delete(urlEqualTo(BASE_PATH + path))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.delete-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(deleteRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
        );
    }

    @Test
    public void shouldRequestSynchronousPostToRemoteService() {

        final String name = "people.create-group";

        final String path = "/groups/" + GROUP_ID.toString();
        final String mimeType = "application/vnd.people.group+json";
        final String bodyPayload = createObjectBuilder()
                .add("groupName", GROUP_NAME)
                .build().toString();

        stubFor(post(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimeType)
                        .withBody(RESPONSE.toDebugStringPrettyPrint())));

        final JsonEnvelope group = envelope()
                .with(metadataOf(randomUUID(), name))
                .withPayloadOf(GROUP_ID.toString(), "groupId")
                .withPayloadOf(GROUP_NAME, "groupName")
                .build();

        final JsonEnvelope response = requester.request(group);

        assertThat(response, jsonEnvelope(
                metadata().withName("people.group"),
                payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.groupName", is(GROUP_NAME)))
                )));
    }

    @Test
    public void shouldRequestSynchronousPutToRemoteService() {

        final String name = "people.update-group";

        final String path = "/groups/" + GROUP_ID.toString();
        final String mimeType = "application/vnd.people.group+json";
        final String bodyPayload = createObjectBuilder()
                .add("groupName", GROUP_NAME)
                .build().toString();

        stubFor(put(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimeType)
                        .withBody(RESPONSE.toDebugStringPrettyPrint())));

        final JsonEnvelope group = envelope()
                .with(metadataOf(randomUUID(), name))
                .withPayloadOf(GROUP_ID.toString(), "groupId")
                .withPayloadOf(GROUP_NAME, "groupName")
                .build();

        final JsonEnvelope response = requester.request(group);

        assertThat(response, jsonEnvelope(
                metadata().withName("people.group"),
                payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.groupName", is(GROUP_NAME)))
                )));
    }

    @Test
    public void shouldRequestSynchronousPatchToRemoteService() {

        final String name = "people.modify-group";

        final String path = "/groups/" + GROUP_ID.toString();
        final String mimeType = "application/vnd.people.group+json";
        final String bodyPayload = createObjectBuilder()
                .add("groupName", GROUP_NAME)
                .build().toString();

        stubFor(patch(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimeType)
                        .withBody(RESPONSE.toDebugStringPrettyPrint())));

        final JsonEnvelope group = envelope()
                .with(metadataOf(randomUUID(), name))
                .withPayloadOf(GROUP_ID.toString(), "groupId")
                .withPayloadOf(GROUP_NAME, "groupName")
                .build();

        final JsonEnvelope response = requester.request(group);

        assertThat(response, jsonEnvelope(
                metadata().withName("people.group"),
                payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.groupName", is(GROUP_NAME)))
                )));
    }
}
