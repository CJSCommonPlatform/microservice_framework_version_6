package uk.gov.justice.services.jmx.system.command.client;

import uk.gov.justice.services.jmx.system.command.client.connection.JMXConnectorFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import javax.inject.Inject;

public class SystemCommanderClientFactory {

    @Inject
    private MBeanConnector mBeanConnector;

    @Inject
    private JMXConnectorFactory jmxConnectorFactory;

    public SystemCommanderClient create(final JmxParameters jmxParameters) {

        return new SystemCommanderClient(
                jmxConnectorFactory.createJmxConnector(jmxParameters),
                mBeanConnector);
    }
}
