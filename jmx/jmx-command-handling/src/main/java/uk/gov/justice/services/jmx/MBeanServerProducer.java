package uk.gov.justice.services.jmx;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.management.MBeanServer;

@ApplicationScoped
public class MBeanServerProducer {

    @Produces
    public MBeanServer mBeanServer() {
        return getPlatformMBeanServer();
    }
}
