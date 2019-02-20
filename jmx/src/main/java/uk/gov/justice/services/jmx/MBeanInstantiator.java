package uk.gov.justice.services.jmx;


import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;

@Singleton
@Startup
public class MBeanInstantiator {

    @Inject
     MBeanRegistry mBeanRegistry;

    private MBeanServer mbeanServer = getPlatformMBeanServer();

    @PostConstruct
    public void registerMBeans() {
        mBeanRegistry.getMBeanMap()
                .forEach((key, value) -> {
                    try {
                        mbeanServer.registerMBean(key, value);
                    } catch (final InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                        throw new MBeanException(String.format("Error during registration MXBean registration: %s", e.getMessage()));
                    }
                });
    }

    @PreDestroy
    public void unregisterMBeans() {
        mBeanRegistry.getMBeanMap()
                .forEach((key, value) -> {
                    try {
                        mbeanServer.unregisterMBean(value);
                    } catch (final InstanceNotFoundException | MBeanRegistrationException e) {
                        throw new MBeanException(String.format("Error during MXBean unregistration: %s", e.getMessage()));
                    }
                });
    }
}
