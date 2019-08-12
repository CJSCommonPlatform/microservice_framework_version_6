package uk.gov.justice.services.jmx;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.mbean.SystemCommander;
import uk.gov.justice.services.jmx.api.name.CommandMBeanNameProvider;
import uk.gov.justice.services.jmx.api.name.ObjectNameException;
import uk.gov.justice.services.jmx.util.ContextNameProvider;

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

import org.slf4j.Logger;

@Startup
@Singleton
public class MBeanInstantiator {

    @Inject
    private MBeanServer mbeanServer;

    @Inject
    private SystemCommander systemCommander;

    @Inject
    private ContextNameProvider contextNameProvider;

    @Inject
    private CommandMBeanNameProvider commandMBeanNameProvider;

    @Inject
    private Logger logger;

    @PostConstruct
    public void registerSystemCommanderMBean() {

        final String contextName = contextNameProvider.getContextName();

        final ObjectName objectName = commandMBeanNameProvider.create(contextName);

        if (!mbeanServer.isRegistered(objectName)) {
            register(objectName);
        }
    }

    @PreDestroy
    public void unregisterMBeans() {

        final String contextName = contextNameProvider.getContextName();
        final ObjectName objectName = commandMBeanNameProvider.create(contextName);

        if (mbeanServer.isRegistered(objectName)) {
            unregister(objectName);
        }
    }

    private void register(final ObjectName objectName) {
        try {

            logger.info(format("Registering %s MBean using name '%s'", SystemCommander.class.getSimpleName(), objectName));

            mbeanServer.registerMBean(systemCommander, objectName);

        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new ObjectNameException(format("Failed to register SystemCommander MBean using object name '%s'", objectName), e);
        }
    }


    private void unregister(final ObjectName objectName) {
        try {
            logger.info(format("Unregistering %s MBean using name '%s'",  SystemCommander.class.getSimpleName(), objectName));
            mbeanServer.unregisterMBean(objectName);
        } catch (final InstanceNotFoundException | MBeanRegistrationException e) {
            throw new ObjectNameException(format("Failed to unregister MBean with object name '%s'", objectName), e);
        }
    }
}

