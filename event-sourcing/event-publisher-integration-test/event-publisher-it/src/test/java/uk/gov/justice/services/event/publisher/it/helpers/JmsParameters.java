package uk.gov.justice.services.event.publisher.it.helpers;

public interface JmsParameters {

    String JMS_USERNAME = "jmsuser";
    String JMS_PASSWORD = "jms@user123";
    String RANDOM_JMS_PORT = System.getProperty("random.jms.port");
    String JMS_BROKER_URL = "tcp://localhost:" + RANDOM_JMS_PORT + "?broker.persistent=false&jms.useAsyncSend=false";

}
