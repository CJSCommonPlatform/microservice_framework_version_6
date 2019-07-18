package uk.gov.justice.services.jmx.system.command.client.connection;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.name.CommandMBeanNameProvider;
import uk.gov.justice.services.jmx.system.command.client.MBeanClientException;

import java.io.IOException;

import javax.inject.Inject;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

public class MBeanConnector {

    @Inject
    private CommandMBeanNameProvider commandMBeanNameProvider;

    @Inject
    private RemoteMBeanFactory remoteMBeanFactory;

    public <T> T connect(final String contextName, final Class<T> mBeanInterface, final JMXConnector jmxConnector) {
        try {
            final ObjectName objectName = commandMBeanNameProvider.create(contextName);
            final MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

            return remoteMBeanFactory.createRemote(connection, objectName, mBeanInterface);
        } catch (final IOException e) {
            throw new MBeanClientException(format("Failed to get remote connection to MBean '%s'", mBeanInterface.getSimpleName()), e);
        }
    }
}
