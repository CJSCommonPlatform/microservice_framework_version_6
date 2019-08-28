package uk.gov.justice.services.jmx.bootstrap;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.bootstrap.blacklist.BlacklistedCommandsFilter;
import uk.gov.justice.services.jmx.command.HandlerMethodValidator;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class SystemCommandProxyResolver {

    private final CdiInstanceResolver cdiInstanceResolver;
    private final SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory;
    private final HandlerMethodValidator handlerMethodValidator;
    private final BlacklistedCommandsFilter blacklistedCommandsFilter;

    public SystemCommandProxyResolver(
            final CdiInstanceResolver cdiInstanceResolver,
            final SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory,
            final HandlerMethodValidator handlerMethodValidator,
            final BlacklistedCommandsFilter blacklistedCommandsFilter) {
        this.cdiInstanceResolver = cdiInstanceResolver;
        this.systemCommandHandlerProxyFactory = systemCommandHandlerProxyFactory;
        this.handlerMethodValidator = handlerMethodValidator;
        this.blacklistedCommandsFilter = blacklistedCommandsFilter;
    }

    public List<SystemCommandHandlerProxy> allCommandProxiesFor(final Bean<?> bean, final BeanManager beanManager, final Set<SystemCommand> blacklistedCommands) {

        final List<SystemCommandHandlerProxy> systemCommandHandlerProxies = new ArrayList<>();

        final Class<?> beanClass = bean.getBeanClass();
        for (final Method method : beanClass.getDeclaredMethods()) {

            if (method.isAnnotationPresent(HandlesSystemCommand.class)) {

                final HandlesSystemCommand handlesSystemCommand = method.getDeclaredAnnotation(HandlesSystemCommand.class);
                final String commandName = handlesSystemCommand.value();

                if (blacklistedCommandsFilter.isSystemCommandAllowed(commandName, blacklistedCommands)) {

                    final Object systemCommandHandler = cdiInstanceResolver.getInstanceOf(beanClass, beanManager);

                    final SystemCommandHandlerProxy systemCommandHandlerProxy = systemCommandHandlerProxyFactory.create(
                            commandName,
                            method,
                            systemCommandHandler,
                            handlerMethodValidator
                    );
                    
                    systemCommandHandlerProxies.add(systemCommandHandlerProxy);
                }
            }
        }

        return systemCommandHandlerProxies;
    }
}
