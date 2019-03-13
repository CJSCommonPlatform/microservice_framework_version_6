package uk.gov.justice.services.core.jmx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.lang.management.ManagementFactory;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class JmxMBeanTest {

    @Module
    @Classes(cdi = true, value = {
            MBeanInstantiator.class,
            MBeanRegistry.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("event-source-api")
                .addServlet("JmxMBeanTest", Application.class.getName());
    }

    @Test
    public void shouldCreateShutterableMbean() throws Exception {
        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("shuttering", "type", Shuttering.class.getSimpleName());
        final MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);

        assertNotNull(mbeanInfo);
        assertThat(mbeanInfo.getOperations()[0].getName(), is("doShutteringRequested"));
        assertThat(mbeanInfo.getOperations()[1].getName(), is("doUnshutteringRequested"));
    }

    @Test
    public void shouldCreateCatchupMbean() throws Exception {
        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("catchup", "type", Catchup.class.getSimpleName());
        final MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);

        assertNotNull(mbeanInfo);
        assertThat(mbeanInfo.getOperations()[0].getName(), is("doCatchupRequested"));
    }
}
