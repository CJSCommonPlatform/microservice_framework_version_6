package uk.gov.justice.services.jmx.bootstrap;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.jmx.command.HandlerMethodValidator;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.command.SystemCommandHandlerProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public class SystemCommandProxyResolver {

    private final CdiInstanceResolver cdiInstanceResolver;
    private final SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory;
    private final HandlerMethodValidator handlerMethodValidator;

    public SystemCommandProxyResolver(
            final CdiInstanceResolver cdiInstanceResolver,
            final SystemCommandHandlerProxyFactory systemCommandHandlerProxyFactory,
            final HandlerMethodValidator handlerMethodValidator) {
        this.cdiInstanceResolver = cdiInstanceResolver;
        this.systemCommandHandlerProxyFactory = systemCommandHandlerProxyFactory;
        this.handlerMethodValidator = handlerMethodValidator;
    }

    public List<SystemCommandHandlerProxy> allCommandProxiesFor(final Bean<?> bean, final BeanManager beanManager) {

        final List<SystemCommandHandlerProxy> systemCommandHandlerProxies = new ArrayList<>();

        final Class<?> beanClass = bean.getBeanClass();
        for (final Method method : beanClass.getDeclaredMethods()) {

            if (method.isAnnotationPresent(HandlesSystemCommand.class)) {

                final Object systemCommandHandler = cdiInstanceResolver.getInstanceOf(beanClass, beanManager);
                final HandlesSystemCommand handlesSystemCommand = method.getDeclaredAnnotation(HandlesSystemCommand.class);

                final SystemCommandHandlerProxy systemCommandHandlerProxy = systemCommandHandlerProxyFactory.create(
                        handlesSystemCommand.value(),
                        method,
                        systemCommandHandler,
                        handlerMethodValidator
                );

                systemCommandHandlerProxies.add(systemCommandHandlerProxy);
            }
        }

        return systemCommandHandlerProxies;
    }
}
