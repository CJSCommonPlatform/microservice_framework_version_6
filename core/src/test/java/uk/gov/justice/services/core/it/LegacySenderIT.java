package uk.gov.justice.services.core.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationException;
import uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.it.util.sender.RecordingSender;
import uk.gov.justice.services.core.jms.JmsDestinations;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Destination;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class LegacySenderIT {

    private static final UUID TEST_SYS_USER_ID = randomUUID();

    @Inject
    @ServiceComponent(COMMAND_CONTROLLER)
    private Sender sender;

    @Inject
    private RecordingJmsEnvelopeSender jmsEnvelopeSender;

    @Inject
    private RecordedJmsDestinationQueries jmsDestinations;


    @Module
    @Classes(cdi = true, value = {
            AccessControlFailureMessageGenerator.class,
            AccessControlService.class,
            AllowAllPolicyEvaluator.class,
            InterceptorChainProcessor.class,
            ComponentDestination.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            RecordedJmsDestinationQueries.class,
            JmsSenderFactory.class,
            PolicyEvaluator.class,
            RecordingJmsEnvelopeSender.class,
            RequesterProducer.class,
            SenderProducer.class,
            ServiceComponentObserver.class,
            LoggerProducer.class,
            TestSystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,

            ObjectMapperProducer.class,
            RethrowingValidationExceptionHandler.class,
            DefaultJsonSchemaValidator.class,
            JsonSchemaLoader.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("jms-legacy-sender-test");
    }

    @Before
    public void before() throws Exception {
        RecordingSender.instance().reset();
    }

    @Test
    public void shouldSendEnvelopeToAJmsDestination() throws Exception {
        final UUID id = randomUUID();
        final String userId = "userId1234";
        final String commandName = "contexta.command.aaa";

        sender.send(envelope().with(metadataOf(id, commandName).withUserId(userId)).withPayloadOf("a", "someField1").build());

        final List<JsonEnvelope> sentEnvelopes = jmsEnvelopeSender.recordedEnvelopes();
        assertThat(sentEnvelopes, hasSize(1));
        assertThat(sentEnvelopes.get(0).metadata().name(), is(commandName));
        assertThat(sentEnvelopes.get(0).metadata().id(), is(id));
        assertThat(sentEnvelopes.get(0).metadata().userId().get(), is(userId));

        final List<Pair<Component, String>> recordedDestinationQueries = jmsDestinations.recordedDestinationQueries();
        assertThat(recordedDestinationQueries, hasSize(1));
        final Component requestedComponent = recordedDestinationQueries.get(0).getLeft();
        final String requestedContextName = recordedDestinationQueries.get(0).getRight();
        assertThat(requestedComponent, is(COMMAND_HANDLER));
        assertThat(requestedContextName, is("contexta"));

    }

    @Test
    public void shouldSendEnvelopeAsAdmin() throws Exception {
        final UUID id = randomUUID();
        final String userId = "userId1234";
        final String commandName = "contexta.command.aaa";

        sender.sendAsAdmin(envelope().with(metadataOf(id, commandName).withUserId(userId)).withPayloadOf("b", "someField1").build());

        final List<JsonEnvelope> sentEnvelopes = jmsEnvelopeSender.recordedEnvelopes();
        assertThat(sentEnvelopes, hasSize(1));
        assertThat(sentEnvelopes.get(0).metadata().name(), is(commandName));
        assertThat(sentEnvelopes.get(0).metadata().id(), is(id));
        assertThat(sentEnvelopes.get(0).metadata().userId().get(), is(TEST_SYS_USER_ID.toString()));

    }

    @Test(expected = EnvelopeValidationException.class)
    public void shouldThrowExceptionIfPayloadDoesNotAdhereToSchema() throws Exception {
        sender.send(envelope().with(metadataWithRandomUUID("contexta.command.aaa")).withPayloadOf("Aaaa", "unknownField").build());
    }


    @ApplicationScoped
    public static class TestSystemUserProvider implements SystemUserProvider {

        @Override
        public Optional<UUID> getContextSystemUserId() {
            return Optional.of(TEST_SYS_USER_ID);
        }
    }

    @ApplicationScoped
    public static class RecordedJmsDestinationQueries implements JmsDestinations {

        private List<Pair<Component, String>> destinationQueries = new LinkedList<>();

        @Override
        public Destination getDestination(final Component component, final String contextName) {
            destinationQueries.add(Pair.of(component, contextName));
            return null;
        }

        public List<Pair<Component, String>> recordedDestinationQueries() {
            return destinationQueries;
        }
    }

    @ApplicationScoped
    public static class RecordingJmsEnvelopeSender extends TestEnvelopeRecorder implements JmsEnvelopeSender {

        @Override
        public void send(final JsonEnvelope envelope, final Destination destination) {
            record(envelope);
        }

        @Override
        public void send(final JsonEnvelope envelope, final String destinationName) {

        }
    }

}
