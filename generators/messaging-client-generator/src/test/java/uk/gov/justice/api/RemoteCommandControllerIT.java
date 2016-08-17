package uk.gov.justice.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcherProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcherProducer;
import uk.gov.justice.services.core.eventbuffer.PassThroughEventBufferService;
import uk.gov.justice.services.core.jms.JmsDestinations;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.test.util.RecordingJmsEnvelopeSender;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@ServiceComponent(COMMAND_API)
public class RemoteCommandControllerIT {
    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .property("httpejbd.port", Integer.toString(port))
                .property(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Inject
    Sender sender;

    @Inject
    RecordingJmsEnvelopeSender envelopeSender;

    @Module
    @Classes(cdi = true, value = {
            AccessControlFailureMessageGenerator.class,
            AccessControlService.class,
            AllowAllPolicyEvaluator.class,
            AsynchronousDispatcherProducer.class,
            ComponentDestination.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            JmsDestinations.class,
            JmsSenderFactory.class,
            PolicyEvaluator.class,
            RecordingJmsEnvelopeSender.class,
            RemoteContextaControllerCommand.class,
            RequesterProducer.class,
            SenderProducer.class,
            ServiceComponentObserver.class,
            SynchronousDispatcherProducer.class,
            PassThroughEventBufferService.class,
            LoggerProducer.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("jms-endpoint-test");
    }

    @Before
    public void setUp() throws Exception {
        envelopeSender.init();
    }

    @Test
    public void shouldPassEnvelopeToEnvelopeSender() throws Exception {
        final String name = "contexta.commanda";
        final UUID id = UUID.randomUUID();
        sender.send(envelope().with(metadataOf(id, name)).build());

        final List<JsonEnvelope> sentEnvelopes = envelopeSender.envelopesSentTo("contexta.controller.command");
        assertThat(sentEnvelopes, hasSize(1));
        assertThat(sentEnvelopes.get(0).metadata().name(), is(name));
        assertThat(sentEnvelopes.get(0).metadata().id(), is(id));
    }
}
