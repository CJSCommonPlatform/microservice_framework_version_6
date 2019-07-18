package uk.gov.justice.services.jmx.system.command.client.connection;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.system.command.client.MBeanClientException;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.security.sasl.SaslException;

public class JMXConnectorFactory {

    @Inject
    private JmxUrlFactory jmxUrlFactory;

    @Inject
    private ConnectorWrapper connectorWrapper;

    @Inject
    private EnvironmentFactory environmentFactory;

    public JMXConnector createJmxConnector(final JmxParameters jmxParameters) {

        final Map<String, Object> environment = environmentFactory.create(jmxParameters);
        final String host = jmxParameters.getHost();
        final int port = jmxParameters.getPort();

        final JMXServiceURL serviceURL = jmxUrlFactory.createUrl(host, port);

        try {
            return connectorWrapper.connect(serviceURL, environment);
        } catch (final SaslException e) {
           throw new JmxAuthenticationException("Jmx authentication failed", e);
        } catch (final IOException e) {
            throw new MBeanClientException(format("Failed to connect to JMX using url '%s'", serviceURL), e);
        }
    }
}
