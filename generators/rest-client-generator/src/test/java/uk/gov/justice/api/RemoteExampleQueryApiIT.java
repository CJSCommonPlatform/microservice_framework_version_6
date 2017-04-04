package uk.gov.justice.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.requester.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;

import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@FrameworkComponent("COMPONENT_ABC")
public class RemoteExampleQueryApiIT {

    private static int port = -1;
    private static final String PEOPLE_QUERY_USER1 = "people.query.user1";
    private static final String MIME_TYPE = format("application/vnd.%s+json", PEOPLE_QUERY_USER1);
    private static final String BASE_PATH = "/rest-client-generator/query/controller/rest/example";
    private static final UUID USER_ID = randomUUID();
    private static final String TEST_SYSTEM_USER_ID = "8d6a96f0-6e8e-11e6-8b77-86f30ca893d3";
    private static final String MOCK_SERVER_PORT = "mock.server.port";

    private static final String PEOPLE_GET_USER1 = "people.get-user1";

    private static final JsonEnvelope RESPONSE = DefaultJsonEnvelope.envelope()
            .with(JsonObjectMetadata.metadataWithRandomUUID("people.get-user1"))
            .withPayloadOf("SUCCESS", "result")
            .build();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9090);
    @Inject
    Requester requester;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty(MOCK_SERVER_PORT, "9090");
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
            AccessControlFailureMessageGenerator.class,
            DefaultAccessControlService.class,
            AllowAllPolicyEvaluator.class,
            BaseUriFactory.class,
            BeanInstantiater.class,
            ContextMatcher.class,
            DefaultServerPortProvider.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            DefaultEnveloper.class,
            InterceptorCache.class,
            InterceptorChainProcessor.class,
            InterceptorChainProcessorProducer.class,
            JndiBasedServiceContextNameProvider.class,
            DefaultJsonObjectEnvelopeConverter.class,
            LoggerProducer.class,
            MockServerPortProvider.class,
            ObjectToJsonValueConverter.class,
            PolicyEvaluator.class,
            RequesterProducer.class,
            DefaultRestClientHelper.class,
            DefaultRestClientProcessor.class,
            ServiceComponentObserver.class,
            StringToJsonObjectConverter.class,
            SystemUserUtil.class,
            TestSystemUserProvider.class,
            RemoteExampleQueryController.class,
            WebTargetFactory.class,
            UtcClock.class,

            EmptySystemUserProvider.class,
            ObjectMapperProducer.class,
            RemoteExampleCommandApi.class,

            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,
            DefaultJsonSchemaValidator.class,
            JsonSchemaLoader.class,
            DefaultTraceLogger.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("rest-client-generator")
                .addServlet("TestApp", Application.class.getName());
    }

    @Before
    public void setUp() throws Exception {

        stubFor(get(urlEqualTo(BASE_PATH + format("/users/%s", USER_ID)))
                .withHeader("Accept", equalTo(MIME_TYPE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MIME_TYPE)
                        .withBody(RESPONSE.toDebugStringPrettyPrint())));
    }

    @Test
    public void shouldSendQueryRemoteServiceAndReturnResponse() {

        final JsonEnvelope query = envelope()
                .with(metadataOf(randomUUID(), PEOPLE_GET_USER1))
                .withPayloadOf(USER_ID, "userId")
                .build();

        final JsonEnvelope response = requester.request(query);
        assertThat(response, jsonEnvelope(
                metadata().withName("people.get-user1"),
                payloadIsJson(withJsonPath("$.result", is("SUCCESS")))));
    }

    @Test
    public void shouldSendQueryWithHttpHeaders() {

        requester.request(envelope()
                .with(metadataOf(randomUUID(), PEOPLE_GET_USER1)
                        .withSessionId("sessionId123")
                        .withUserId("usrId12345")
                        .withClientCorrelationId("correlationID23456")
                        .withCausation(
                                UUID.fromString("391de66a-4e7c-11e6-beb8-9e71128cae77"),
                                UUID.fromString("391ded4a-4e7c-11e6-beb8-9e71128cae77"))

                )
                .withPayloadOf(USER_ID, "userId")
                .build());

        verify(getRequestedFor(urlEqualTo(format("%s/users/%s", BASE_PATH, USER_ID)))
                .withHeader("CPPSID", equalTo("sessionId123"))
                .withHeader("CJSCPPUID", equalTo("usrId12345"))
                .withHeader("CPPCLIENTCORRELATIONID", equalTo("correlationID23456"))
                .withHeader("CPPCAUSATION", equalTo("391de66a-4e7c-11e6-beb8-9e71128cae77,391ded4a-4e7c-11e6-beb8-9e71128cae77"))

        );
    }

    @Test
    public void shouldSubstituteSystemUserIdWhenSendingAsAdmin() {

        requester.requestAsAdmin(envelope()
                .with(metadataOf(randomUUID(), PEOPLE_GET_USER1)
                        .withUserId("usrId12345")
                )
                .withPayloadOf(USER_ID, "userId")
                .build());

        verify(getRequestedFor(urlEqualTo(format("%s/users/%s", BASE_PATH, USER_ID)))
                .withHeader("CJSCPPUID", equalTo(TEST_SYSTEM_USER_ID))
        );
    }

    @Test(expected = AccessControlViolationException.class)
    public void shouldThrowAccessControlExceptionInCaseOf403Response() {

        final JsonEnvelope query = envelope()
                .with(metadataOf(randomUUID(), PEOPLE_GET_USER1))
                .withPayloadOf(USER_ID, "userId")
                .build();

        final String path = format("/users/%s", USER_ID);
        final String mimeType = format("application/vnd.%s+json", PEOPLE_QUERY_USER1);

        stubFor(get(urlEqualTo(BASE_PATH + path))
                .withHeader("Accept", equalTo(mimeType))
                .willReturn(aResponse()
                        .withStatus(403)));

        requester.request(query);
    }

    @Alternative
    @Priority(2)
    public static class TestSystemUserProvider implements SystemUserProvider {

        @Override
        public Optional<UUID> getContextSystemUserId() {
            return Optional.of(UUID.fromString(TEST_SYSTEM_USER_ID));
        }
    }
}
