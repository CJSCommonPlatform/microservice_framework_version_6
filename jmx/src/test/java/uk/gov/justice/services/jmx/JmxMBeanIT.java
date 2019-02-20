package uk.gov.justice.services.jmx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.ApplicationStateController;
import uk.gov.justice.services.core.lifecycle.shuttering.Shutterable;
import uk.gov.justice.services.core.lifecycle.shuttering.ShutteringListener;

import java.lang.management.ManagementFactory;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class JmxMBeanIT {

    @Module
    @Classes(cdi = true, value = {
            MBeanInstantiator.class,
            DefaultShutteringMBean.class,
            ShutteringMBean.class,
            Shutterable.class,
            ShutteringListener.class,
            ApplicationStateController.class,
            UtcClock.class,
            MBeanRegistry.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("event-source-api")
                .addServlet("JmxMBeanIT", Application.class.getName());
    }

    @Test
    public void shouldCreateShutterableMbean() throws Exception {
        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("shuttering", "type", DefaultShutteringMBean.class.getSimpleName());
        final MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);

        assertNotNull(mbeanInfo);
        assertThat(mbeanInfo.getOperations()[0].getName(), is("doShutteringRequested"));
        assertThat(mbeanInfo.getOperations()[1].getName(), is("doUnshutteringRequested"));
    }

    @Test
    public void shouldCreateCatchupMbean() throws Exception {
        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("catchup", "type", DefaultCatchupMBean.class.getSimpleName());
        final MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);

        assertNotNull(mbeanInfo);
        assertThat(mbeanInfo.getOperations()[0].getName(), is("doCatchupRequested"));
    }
}
