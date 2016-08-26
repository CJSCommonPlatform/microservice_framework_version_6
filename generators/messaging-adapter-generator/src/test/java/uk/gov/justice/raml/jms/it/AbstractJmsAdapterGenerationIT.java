package uk.gov.justice.raml.jms.it;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import java.util.Properties;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.JsonObject;
import javax.naming.NamingException;

import org.apache.cxf.common.i18n.Exception;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractJmsAdapterGenerationIT {
    private static int port = -1;
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

    @Before
    public void setup() throws Exception, JMSException, NamingException {
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @After
    public void after() throws JMSException {
        connection.close();
        session.close();
    }

    protected void sendEnvelope(String metadataId, String commandName, Destination queue) throws JMSException {
        sendEnvelope(metadataId, commandName, queue, createObjectBuilder().build());
    }

    protected void sendEnvelope(String metadataId, String commandName, Destination queue, JsonObject payload) throws JMSException {
        TextMessage message = session.createTextMessage();
        message.setText(envelope().with(metadataOf(metadataId, commandName)).toJsonString());
        message.setStringProperty("CPPNAME", commandName);
        try (MessageProducer producer = session.createProducer(queue)) {
            producer.send(message);
        }
    }
}
