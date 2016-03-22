package uk.gov.justice.raml.jms.it;

import org.apache.cxf.common.i18n.Exception;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.justice.api.StructureControllerCommandsJmsListener;
import uk.gov.justice.api.StructureEventsJmsListener;
import uk.gov.justice.api.StructureHandlerCommandsJmsListener;
import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.common.converter.JsonObjectConverter;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.NamingException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static com.jayway.awaitility.Awaitility.await;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for the generated JAX-RS classes.
 */
@RunWith(ApplicationComposer.class)
public class JmsEndpointGenerationIT {

    private static int port = -1;

    @Inject
    DummyDispatcher dispatcher;

    @Resource(name = "structure.handler.commands")
    private Queue commandHandlerDestination;

    @Resource(name = "structure.controller.commands")
    private Queue commandControllerDestination;

    @Resource(name = "structure.events")
    private Topic eventsListenerDestination;

    @Resource
    private ConnectionFactory factory;

    private Connection connection;

    private Session session;

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

    @Module
    @Classes(cdi = true, value = {
            JmsProcessor.class,
            DummyDispatcher.class,
            StructureControllerCommandsJmsListener.class,
            StructureEventsJmsListener.class,
            StructureHandlerCommandsJmsListener.class,
            EnvelopeConverter.class,
            JsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("jms-endpoint-test");
    }

    @Before
    public void setup() throws Exception, JMSException, NamingException {
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        cleanQueue(commandControllerDestination);
        cleanQueue(commandHandlerDestination);
        cleanQueue(eventsListenerDestination);

        dispatcher.reset();
    }

    @After
    public void after() throws JMSException {
        connection.close();
        session.close();
    }


    @Test
    public void commandControllerHandlerShouldReceiveCommandA() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d65";
        String commandName = "structure.commands.commanda";
        sendEnvelope(metadataId, commandName, commandControllerDestination);

        await().until(() -> dispatcher.receivedEnvelope() != null);

        Envelope receivedEnvelope = dispatcher.receivedEnvelope();
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(commandName));

    }

    @Test
    public void commandControllerHandlerShouldReceiveCommandB() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d11";
        String commandName = "structure.commands.commandb";

        sendEnvelope(metadataId, commandName, commandControllerDestination);

        await().until(() -> dispatcher.receivedEnvelope() != null);

        Envelope receivedEnvelope = dispatcher.receivedEnvelope();
        assertThat(receivedEnvelope.metadata().name(), is(commandName));
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));

    }

    @Test
    public void commandControllerHandlerShouldNotReceiveACommandUnspecifiedInMessageSelector()
            throws JMSException, InterruptedException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d11";
        String commandName = "structure.commands.commandc";

        sendEnvelope(metadataId, commandName, commandControllerDestination);

        Thread.sleep(300);

        assertThat(dispatcher.receivedEnvelope(), nullValue());

    }

    @Test
    public void commandHandlerHandlerShouldReceiveCommandA() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d61";
        String commandName = "structure.commands.cmdaa";
        sendEnvelope(metadataId, commandName, commandHandlerDestination);

        await().until(() -> dispatcher.receivedEnvelope() != null);

        Envelope receivedEnvelope = dispatcher.receivedEnvelope();
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(commandName));

    }

    @Test
    public void commandHandlerHandlerShouldNotReceiveACommandUnspecifiedInMessageSelector()
            throws JMSException, InterruptedException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d12";
        String commandName = "structure.handler.cmdcc";

        sendEnvelope(metadataId, commandName, commandHandlerDestination);

        Thread.sleep(300);

        assertThat(dispatcher.receivedEnvelope(), nullValue());

    }

    @Test
    public void eventListenerHandlerShouldReceiveEventA() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d20";
        String commandName = "structure.events.eventaa";
        sendEnvelope(metadataId, commandName, eventsListenerDestination);

        await().until(() -> dispatcher.receivedEnvelope() != null);

        Envelope receivedEnvelope = dispatcher.receivedEnvelope();
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(commandName));

    }

    @Test
    public void eventListenerHandlerShouldNotReceiveACommandUnspecifiedInMessageSelector()
            throws JMSException, InterruptedException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d21";
        String commandName = "structure.events.eventcc";

        sendEnvelope(metadataId, commandName, eventsListenerDestination);

        Thread.sleep(300);

        assertThat(dispatcher.receivedEnvelope(), nullValue());

    }

    private void sendEnvelope(String metadataId, String commandName, Destination queue) throws JMSException {
        TextMessage message = session.createTextMessage();
        message.setText(envelopeJsonWith(metadataId, commandName));
        message.setStringProperty("CPPNAME", commandName);
        try (MessageProducer producer = session.createProducer(queue)) {
            producer.send(message);
        }
    }

    private String envelopeJsonWith(String metadataId, String commandName) {
        return createObjectBuilder()
                .add("_metadata", createObjectBuilder()
                        .add("id", metadataId)
                        .add("name", commandName))
                .build().toString();
    }

    private void cleanQueue(Destination queue) throws JMSException {
        try (MessageConsumer consumer = session.createConsumer(queue)) {
            Optional<Message> message;
            do {
                message = Optional.ofNullable(consumer.receiveNoWait());
                if (message.isPresent()) {
                    message.get().acknowledge();
                }
            } while (message.isPresent());
        }
    }

}