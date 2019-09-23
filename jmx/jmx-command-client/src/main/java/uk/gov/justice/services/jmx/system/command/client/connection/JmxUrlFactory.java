package uk.gov.justice.services.jmx.system.command.client.connection;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.system.command.client.MBeanClientConnectionException;

import java.net.MalformedURLException;

import javax.management.remote.JMXServiceURL;

public class JmxUrlFactory {

    public JMXServiceURL createUrl(final String hostName, final int port) {

        final String urlString = "service:jmx:remote+http://" + hostName + ":" + port;

        try {
            return new JMXServiceURL(urlString);
        } catch (final MalformedURLException e) {
            throw new MBeanClientConnectionException(format("Failed to create JMX service url using host '%s' and port %d", hostName, port), e);
        }
    }
}
