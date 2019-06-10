package uk.gov.justice.services.jmx.system.command.client;

import uk.gov.justice.services.jmx.system.command.client.build.ObjectFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import javax.management.remote.JMXConnector;

public class SystemCommanderClientFactory {

    private final ObjectFactory objectFactory;

    public SystemCommanderClientFactory() {
        this(new ObjectFactory());
    }

    public SystemCommanderClientFactory(final ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public SystemCommanderClient create(final String hostName, final int port) {

        final MBeanConnector mBeanConnector = objectFactory.mBeanConnector();
        final JMXConnector jmxConnector = objectFactory.jmxConnectorFactory().createJMXConnector(hostName, port);

        return objectFactory.systemCommanderClient(mBeanConnector, jmxConnector);
    }
}
