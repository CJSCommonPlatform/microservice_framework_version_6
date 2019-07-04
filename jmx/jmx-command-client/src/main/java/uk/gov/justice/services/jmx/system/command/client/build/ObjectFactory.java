package uk.gov.justice.services.jmx.system.command.client.build;

import uk.gov.justice.services.jmx.ObjectNameFactory;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClient;
import uk.gov.justice.services.jmx.system.command.client.connection.ConnectorWrapper;
import uk.gov.justice.services.jmx.system.command.client.connection.EnvironmentFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JMXConnectorFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxUrlFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;
import uk.gov.justice.services.jmx.system.command.client.connection.RemoteMBeanFactory;

import javax.management.remote.JMXConnector;

public class ObjectFactory {

    public JmxUrlFactory jmxUrlFactory() {
        return new JmxUrlFactory();
    }

    public ConnectorWrapper connectorWrapper() {
        return new ConnectorWrapper();
    }

    public JMXConnectorFactory jmxConnectorFactory() {
        return new JMXConnectorFactory(jmxUrlFactory(), connectorWrapper(), environmentFactory());
    }

    public MBeanConnector mBeanConnector() {
        return new MBeanConnector(
                objectNameFactory(),
                remoteMBeanFactory());
    }

    public EnvironmentFactory environmentFactory() {
        return new EnvironmentFactory();
    }

    public RemoteMBeanFactory remoteMBeanFactory() {
        return new RemoteMBeanFactory();
    }

    public ObjectNameFactory objectNameFactory() {
        return new ObjectNameFactory();
    }

    public SystemCommanderClient systemCommanderClient(final MBeanConnector mBeanConnector, final JMXConnector jmxConnector) {

        return new SystemCommanderClient(
                jmxConnector,
                mBeanConnector
        );
    }
}
