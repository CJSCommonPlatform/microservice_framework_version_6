package uk.gov.justice.services.jmx.system.command.client.connection;

import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class ConnectorWrapper {

    public JMXConnector connect(final JMXServiceURL serviceURL) throws IOException {
        final Map<String, Object> environment = emptyMap();
        return JMXConnectorFactory.connect(serviceURL, environment);
    }
}
