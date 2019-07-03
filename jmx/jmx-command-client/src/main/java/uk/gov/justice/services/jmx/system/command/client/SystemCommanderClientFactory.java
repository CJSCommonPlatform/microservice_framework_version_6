package uk.gov.justice.services.jmx.system.command.client;

import uk.gov.justice.services.jmx.system.command.client.build.ObjectFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;

public class SystemCommanderClientFactory {

    private static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    private final ObjectFactory objectFactory;

    public SystemCommanderClientFactory() {
        this(new ObjectFactory());
    }

    public SystemCommanderClientFactory(final ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public SystemCommanderClient create(final String hostName, final int port) {
        return create(hostName, port, EMPTY_MAP);
    }

    public SystemCommanderClient create(final String hostName, final int port, final String username, final String password) {

        final Map<String, Object> environmentWithCredentials = objectFactory
                .credentialsFactory()
                .create(username, password);

        return create(hostName, port, environmentWithCredentials);
    }

    private SystemCommanderClient create(final String hostName, final int port, final Map<String, Object> environment) {

        final MBeanConnector mBeanConnector = objectFactory.mBeanConnector();
        final JMXConnector jmxConnector = objectFactory.jmxConnectorFactory().createJmxConnector(hostName, port, environment);

        return objectFactory.systemCommanderClient(mBeanConnector, jmxConnector);
    }
}
