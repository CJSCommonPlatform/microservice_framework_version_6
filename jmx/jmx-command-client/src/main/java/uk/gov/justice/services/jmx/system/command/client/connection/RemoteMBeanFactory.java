package uk.gov.justice.services.jmx.system.command.client.connection;

import static javax.management.JMX.newMBeanProxy;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class RemoteMBeanFactory {

    public <T> T createRemote(final MBeanServerConnection connection, final ObjectName objectName, final Class<T> mBeanInterface) {
        return newMBeanProxy(connection, objectName, mBeanInterface, true);
    }

}
