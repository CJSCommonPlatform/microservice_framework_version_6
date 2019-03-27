package uk.gov.justice.services.jmx;

import static java.lang.String.format;

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

@Startup
@Singleton
public class MBeanInstantiator {

    @Inject
    private MBeanRegistry mBeanRegistry;

    @Inject
    private MBeanServer mbeanServer;

    @PostConstruct
    public void registerMBeans() {
        mBeanRegistry.getMBeanMap()
                .forEach((key, value) -> {
                    try {
                        mbeanServer.registerMBean(key, value);
                    } catch (final InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                        throw new MBeanException(format("MXBean registration failed for key '%s', value '%s'", key, value), e);
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
                        throw new MBeanException(format("MXBean unregistration failed for key '%s', value '%s'", key, value), e);
                    }
                });
    }
}

