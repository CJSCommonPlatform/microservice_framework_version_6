package uk.gov.justice.services.test.utils.helper;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.List;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static javax.management.JMX.newMBeanProxy;
import static javax.management.remote.JMXConnectorFactory.connect;

public class MBeanHelper {

    @Inject
    private Logger logger;

    private static final String HOST = "localhost";
    private static final String RANDOM_MANAGEMENT_PORT = "random.management.port";

    public <T> T getMbeanProxy(final ObjectName objectName, final MBeanServerConnection connection, final Class<T> interfaceClass) {
        return newMBeanProxy(connection, objectName, interfaceClass, true);
    }

    public void getMbeanDomains(final MBeanServerConnection connection) throws IOException {
        final String [] domains = connection.getDomains();
        final List<String> mbeanDomains = asList(domains);

        logger.debug("MBean Domains: ");
        mbeanDomains.forEach(mbeanDomain -> logger.debug(mbeanDomain));
    }

    public void getMbeanOperations(final ObjectName objectName, final MBeanServerConnection connection) throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        final MBeanInfo mBeanInfo = connection.getMBeanInfo(objectName);
        final MBeanOperationInfo[] operations = mBeanInfo.getOperations();
        final List<MBeanOperationInfo> mbeanOperations = asList(operations);

        logger.debug("MBean Operations: ");
        mbeanOperations.forEach(mBeanOperationInfo -> logger.debug(mBeanOperationInfo.getName()));
    }

    public JMXConnector getJMXConnector() throws IOException {
        final int managementPort = Integer.valueOf(getProperty(RANDOM_MANAGEMENT_PORT));

        final String urlString =
                getProperty("jmx.service.url","service:jmx:remote+http://" + HOST + ":" + managementPort);
        final JMXServiceURL serviceURL = new JMXServiceURL(urlString);

        return connect(serviceURL, null);
    }
}
