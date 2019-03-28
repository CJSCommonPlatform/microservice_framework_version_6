package uk.gov.justice.services.test.utils.helper;

import uk.gov.justice.services.jmx.ShutteringMBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMX;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBeanHelper {

    @Inject
    Logger LOGGER = LoggerFactory.getLogger(MBeanHelper.class.getName());

    private static final String HOST = "localhost";
    private static final String RANDOM_MANAGEMENT_PORT = "random.management.port";

    public ShutteringMBean getMbeanProxy(ObjectName objectName, final MBeanServerConnection connection) {
        return JMX.newMBeanProxy(connection, objectName, ShutteringMBean.class, true);
    }

    public void getMbeanDomains(final MBeanServerConnection connection) throws IOException {
        final String [] domains = connection.getDomains();
        final List<String> mbeanDomains = Arrays.asList(domains);

        LOGGER.info("MBean Domains: ");
        mbeanDomains.forEach(mbeanDomain -> LOGGER.info(mbeanDomain));
    }

    public void getMbeanOperations(ObjectName objectName, final MBeanServerConnection connection) throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        final MBeanInfo mBeanInfo = connection.getMBeanInfo(objectName);
        final MBeanOperationInfo[] operations = mBeanInfo.getOperations();
        final List<MBeanOperationInfo> mbeanOperations = Arrays.asList(operations);

        LOGGER.info("MBean Operations: ");
        mbeanOperations.forEach(mBeanOperationInfo -> LOGGER.info(mBeanOperationInfo.getName()));
    }

    public JMXConnector getJMXConnector() throws IOException {
        final int managementPort = Integer.valueOf(System.getProperty(RANDOM_MANAGEMENT_PORT));

        final String urlString =
                System.getProperty("jmx.service.url","service:jmx:remote+http://" + HOST + ":" + managementPort);
        final JMXServiceURL serviceURL = new JMXServiceURL(urlString);

        return JMXConnectorFactory.connect(serviceURL, null);
    }
}
