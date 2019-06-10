package uk.gov.justice.services.jmx.system.command.client.connection;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.system.command.client.MBeanClientException;

import java.io.IOException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

public class JMXConnectorFactory {

    private final JmxUrlFactory jmxUrlFactory;
    private final ConnectorWrapper connectorWrapper;

    public JMXConnectorFactory(final JmxUrlFactory jmxUrlFactory, final ConnectorWrapper connectorWrapper) {
        this.jmxUrlFactory = jmxUrlFactory;
        this.connectorWrapper = connectorWrapper;
    }

    public JMXConnector createJMXConnector(final String host, final int port) {

        final JMXServiceURL serviceURL = jmxUrlFactory.createUrl(host, port);

        try {
            return connectorWrapper.connect(serviceURL);
        } catch (final IOException e) {
            throw new MBeanClientException(format("Failed to connect to JMX using url '%s'", serviceURL), e);
        }
    }
}
