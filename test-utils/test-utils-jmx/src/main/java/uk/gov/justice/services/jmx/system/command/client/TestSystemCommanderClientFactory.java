package uk.gov.justice.services.jmx.system.command.client;

import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import javax.management.remote.JMXConnector;

public class TestSystemCommanderClientFactory {

    private final ObjectFactory objectFactory;

    public TestSystemCommanderClientFactory() {
        this(new ObjectFactory());
    }

    public TestSystemCommanderClientFactory(final ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public SystemCommanderClient create(final JmxParameters jmxParameters) {

        final MBeanConnector mBeanConnector = objectFactory.mBeanConnector();
        final JMXConnector jmxConnector = objectFactory.jmxConnectorFactory().createJmxConnector(jmxParameters);

        return objectFactory.systemCommanderClient(mBeanConnector, jmxConnector);
    }
}
