package uk.gov.justice.services.jmx.system.command.client;

import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.jmx.api.name.ObjectNameFactory;
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

        final JMXConnectorFactory jmxConnectorFactory = new JMXConnectorFactory();

        setField(jmxConnectorFactory, "jmxUrlFactory", jmxUrlFactory());
        setField(jmxConnectorFactory, "connectorWrapper", connectorWrapper());
        setField(jmxConnectorFactory, "environmentFactory", environmentFactory());

        return jmxConnectorFactory;
    }

    public MBeanConnector mBeanConnector() {

        final MBeanConnector mBeanConnector = new MBeanConnector();

        setField(mBeanConnector, "objectNameFactory", objectNameFactory());
        setField(mBeanConnector, "remoteMBeanFactory", remoteMBeanFactory());

        return mBeanConnector;
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
        return new SystemCommanderClient(jmxConnector, mBeanConnector);
    }
}
