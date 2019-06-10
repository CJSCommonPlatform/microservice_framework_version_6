package uk.gov.justice.services.jmx;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.command.SystemCommander;

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
import javax.management.ObjectName;

@Startup
@Singleton
public class MBeanInstantiator {

    private static final String SYSTEM_COMMANDER_DOMAIN_NAME = "systemCommander";

    private static final String SYSTEM_COMMANDER_BEAN = "SystemCommander";
    private static final String OBJECT_NAME_KEY = "type";

    @Inject
    private MBeanServer mbeanServer;

    @Inject
    private SystemCommander systemCommander;

    @Inject
    private ObjectNameFactory objectNameFactory;

    @PostConstruct
    public void registerSystemCommanderMBean() {

        final ObjectName objectName = objectNameFactory.create(SYSTEM_COMMANDER_DOMAIN_NAME, OBJECT_NAME_KEY, SYSTEM_COMMANDER_BEAN);

        try {
            mbeanServer.registerMBean(systemCommander, objectName);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new MBeanException(format("Failed to register SystemCommander MBean using object name '%s'", objectName), e);
        }
    }

    @PreDestroy
    public void unregisterMBeans() {

        final ObjectName objectName = objectNameFactory.create(SYSTEM_COMMANDER_DOMAIN_NAME, OBJECT_NAME_KEY, SYSTEM_COMMANDER_BEAN);

        try {
            mbeanServer.unregisterMBean(objectName);
        } catch (final InstanceNotFoundException | MBeanRegistrationException e) {
            throw new MBeanException(format("Failed to unregister MBean with object name '%s'", objectName), e);
        }
    }
}

