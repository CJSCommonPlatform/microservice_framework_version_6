package uk.gov.justice.raml.jms.it;

import static com.jayway.awaitility.Awaitility.await;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.UUID;

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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.gov.justice.api.StructureControllerCommandsJmsListener;
import uk.gov.justice.raml.jms.it.handler.CommandApiHandler;
import uk.gov.justice.raml.jms.it.handler.CommandControllerHandler;
import uk.gov.justice.raml.jms.it.handler.CommandHandlerHandler;
import uk.gov.justice.raml.jms.it.handler.EventListenerHandler;
import uk.gov.justice.services.messaging.Envelope;

@RunWith(Arquillian.class)
public class JmsEndpointGenereationIT {

    private Session session;
    private Connection connection;
    
    @Resource(mappedName = "java:/ConnectionFactory")
    ConnectionFactory factory;

    @Resource(mappedName = "structure.api.commands")
    Queue commandApiDestination;
    
    @Resource(mappedName = "structure.controller.commands")
    Queue commandControllerDestination;

    @Resource(mappedName = "structure.handler.commands")
    Destination commandHandlerDestination;

    @Resource(mappedName = "structure.events")
    Destination eventsListenerDestination;

    @Inject
    CommandApiHandler commandApiHandler;
    
    @Inject
    CommandControllerHandler commandControllerHandler;

    @Inject
    CommandHandlerHandler commandHandlerHandler;

    @Inject
    EventListenerHandler eventListenerHandler;



    @Test
    public void apiCommandsHandlerShouldReceiveCommandA() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d33";
        String commandName = "structure.commands.commanda";
        sendEnvelope(metadataId, commandName, commandApiDestination);

        await().until(() -> commandApiHandler.receivedEnvelope() != null);

        Envelope receivedEnvelope = commandApiHandler.receivedEnvelope();
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(commandName));

    }

    @Test
    public void apiCommandsHandlerShouldReceiveCommandB() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d34";
        String commandName = "structure.commands.commandb";

        sendEnvelope(metadataId, commandName, commandApiDestination);

        await().until(() -> commandApiHandler.receivedEnvelope() != null);

        Envelope receivedEnvelope = commandApiHandler.receivedEnvelope();
        assertThat(receivedEnvelope.metadata().name(), is(commandName));
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));

    }

    @Test
    public void apiCommandsHandlerShouldNotReceiveACommandUnspecifiedInMessageSelector()
            throws JMSException, InterruptedException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d35";
        String commandName = "structure.commands.commandc";

        sendEnvelope(metadataId, commandName, commandApiDestination);

        Thread.sleep(300);

        assertThat(commandApiHandler.receivedEnvelope(), nullValue());
    }

    
    @Test
    public void commandControllerHandlerShouldReceiveCommandA() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d65";
        String commandName = "structure.commands.commanda";
        sendEnvelope(metadataId, commandName, commandControllerDestination);

        await().until(() -> commandControllerHandler.receivedEnvelope() != null);

        Envelope receivedEnvelope = commandControllerHandler.receivedEnvelope();
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(commandName));

    }

    @Test
    public void commandControllerHandlerShouldReceiveCommandB() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d11";
        String commandName = "structure.commands.commandb";

        sendEnvelope(metadataId, commandName, commandControllerDestination);

        await().until(() -> commandControllerHandler.receivedEnvelope() != null);

        Envelope receivedEnvelope = commandControllerHandler.receivedEnvelope();
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

        assertThat(commandControllerHandler.receivedEnvelope(), nullValue());
    }
    
    @Test
    public void commandHandlerHandlerShouldReceiveCommandA() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d61";
        String commandName = "structure.commands.cmdaa";
        sendEnvelope(metadataId, commandName, commandHandlerDestination);

        await().until(() -> commandHandlerHandler.receivedEnvelope() != null);

        Envelope receivedEnvelope = commandHandlerHandler.receivedEnvelope();
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

        assertThat(commandHandlerHandler.receivedEnvelope(), nullValue());
    }

    @Test
    public void eventListenerHandlerShouldReceiveEventA() throws JMSException {

        String metadataId = "861c9430-7bc6-4bf0-b549-6534394b8d20";
        String commandName = "structure.events.eventaa";
        sendEnvelope(metadataId, commandName, eventsListenerDestination);

        await().until(() -> eventListenerHandler.receivedEnvelope() != null);

        Envelope receivedEnvelope = eventListenerHandler.receivedEnvelope();
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

        assertThat(eventListenerHandler.receivedEnvelope(), nullValue());
    }
    

    @Deployment
    public static WebArchive createDeployment() {
        File[] dependencies = Maven.configureResolver().workOffline().withMavenCentralRepo(true)
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();
        File[] testDependencies = Maven.resolver().loadPomFromFile("pom.xml").importTestDependencies()
                .resolve("com.jayway.awaitility:awaitility").withTransitivity().asFile();
    
        return ShrinkWrap.create(WebArchive.class, "jms-endpoint-test.war")
                .addPackage(StructureControllerCommandsJmsListener.class.getPackage())
                .addPackage(CommandControllerHandler.class.getPackage())
                .addPackage(JmsEndpointGenereationIT.class.getPackage())
                .addAsLibraries(dependencies)
                .addAsLibraries(testDependencies)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    @Before
    public void before() throws JMSException {
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        cleanQueue(commandApiDestination);
        cleanQueue(commandControllerDestination);
        cleanQueue(commandHandlerDestination);
        cleanQueue(eventsListenerDestination);
        commandApiHandler.reset();
        commandControllerHandler.reset();
        commandHandlerHandler.reset();
        eventListenerHandler.reset();
    }

    @After
    public void after() throws JMSException {
        connection.close();
        session.close();
    }
    

    protected void sendEnvelope(String metadataId, String commandName, Destination queue) throws JMSException {
        TextMessage message = session.createTextMessage();
        message.setText(envelopeJsonWith(metadataId, commandName));
        message.setStringProperty("CPPNAME", commandName);
        producerOf(queue).send(message);
    }

    private String envelopeJsonWith(String metadataId, String commandName) {
        String jsonString = createObjectBuilder()
                .add("_metadata", createObjectBuilder()
                        .add("id", metadataId)
                        .add("name", commandName))
                .build().toString();
        return jsonString;
    }

    protected void cleanQueue(Destination queue) throws JMSException {
        MessageConsumer consumer = session.createConsumer(queue);
        Message message = null;
        do {
            message = consumer.receiveNoWait();
            if (message != null) {
                message.acknowledge();
            }
        } while (message != null);
    }

    protected MessageProducer producerOf(Destination queue) throws JMSException {
        return session.createProducer(queue);
    }

}
